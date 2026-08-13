package vip.appap.suxin.module.system.service;

import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.core.KeyValue;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.dal.dataobject.SmsChannelDO;
import vip.appap.suxin.module.system.dal.dataobject.SmsTemplateDO;
import vip.appap.suxin.module.system.framework.sms.core.client.SmsClient;
import vip.appap.suxin.module.system.framework.sms.core.client.dto.SmsReceiveRespDTO;
import vip.appap.suxin.module.system.framework.sms.core.client.dto.SmsSendRespDTO;
import vip.appap.suxin.module.system.mq.message.sms.SmsSendMessage;
import vip.appap.suxin.module.system.mq.producer.sms.SmsProducer;
import vip.appap.suxin.module.system.service.partner.PartnerUserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomLongId;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomPojo;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomPojoList;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomString;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_SEND_MOBILE_NOT_EXISTS;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_SEND_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SmsSendServiceImpl} 的单元测试
 *
 * 覆盖用户手机号解析、模板/渠道状态、参数缺失、发送成功/失败、回执成功/失败
 */
public class SmsSendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SmsSendServiceImpl smsSendService;

    @Mock
    private PartnerService partnerService;
    @Mock
    private PartnerUserService partnerUserService;
    @Mock
    private SmsChannelService smsChannelService;
    @Mock
    private SmsTemplateService smsTemplateService;
    @Mock
    private SmsLogService smsLogService;
    @Mock
    private SmsProducer smsProducer;

    private SmsTemplateDO mockTemplate(Integer status) {
        return randomPojo(SmsTemplateDO.class, o -> {
            o.setStatus(status);
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
    }

    private SmsChannelDO mockChannel() {
        return randomPojo(SmsChannelDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
    }

    @Test
    public void testSendSingleSmsToAdmin() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "123456")
                .put("op", "login").build();
        // mock partnerService 的方法
        PartnerDO partner = randomPojo(PartnerDO.class, o -> o.setMobile("15601691300"));
        when(partnerService.getPartner(eq(userId))).thenReturn(partner);

        // mock SmsTemplateService 的方法
        SmsTemplateDO template = mockTemplate(CommonStatusEnum.ENABLE.getStatus());
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = mockChannel();
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(partner.getMobile()), eq(userId), eq(UserTypeEnum.ADMIN.getValue()),
                eq(Boolean.TRUE), eq(template), eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSmsToAdmin(null, userId, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(partner.getMobile()),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "123456"), new KeyValue<>("op", "login"))));
    }

    @Test
    public void testSendSingleSmsToMember() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "123456")
                .put("op", "login").build();
        // mock partnerUserService 的方法
        String mobile = "15601691300";
        when(partnerUserService.getMemberUserMobile(eq(userId))).thenReturn(mobile);

        // mock SmsTemplateService 的方法
        SmsTemplateDO template = mockTemplate(CommonStatusEnum.ENABLE.getStatus());
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = mockChannel();
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(mobile), eq(userId), eq(UserTypeEnum.MEMBER.getValue()),
                eq(Boolean.TRUE), eq(template), eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSmsToMember(null, userId, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(mobile),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "123456"), new KeyValue<>("op", "login"))));
    }

    /**
     * 发送成功，当短信模板开启时
     */
    @Test
    public void testSendSingleSms_successWhenSmsTemplateEnable() {
        // 准备参数
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "123456")
                .put("op", "login").build();
        // mock SmsTemplateService 的方法
        SmsTemplateDO template = mockTemplate(CommonStatusEnum.ENABLE.getStatus());
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = mockChannel();
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(mobile), eq(userId), eq(userType), eq(Boolean.TRUE), eq(template),
                eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSms(mobile, userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer).sendSmsSendMessage(eq(smsLogId), eq(mobile),
                eq(template.getChannelId()), eq(template.getApiTemplateId()),
                eq(Lists.newArrayList(new KeyValue<>("code", "123456"), new KeyValue<>("op", "login"))));
    }

    /**
     * 模板禁用时：只记录日志，不发送
     */
    @Test
    public void testSendSingleSms_successWhenSmsTemplateDisable() {
        // 准备参数
        String mobile = randomString();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "123456")
                .put("op", "login").build();
        // mock SmsTemplateService 的方法
        SmsTemplateDO template = mockTemplate(CommonStatusEnum.DISABLE.getStatus());
        when(smsTemplateService.getSmsTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String content = randomString();
        when(smsTemplateService.formatSmsTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock SmsChannelService 的方法
        SmsChannelDO smsChannel = mockChannel();
        when(smsChannelService.getSmsChannel(eq(template.getChannelId()))).thenReturn(smsChannel);
        // mock SmsLogService 的方法
        Long smsLogId = randomLongId();
        when(smsLogService.createSmsLog(eq(mobile), eq(userId), eq(userType), eq(Boolean.FALSE), eq(template),
                eq(content), eq(templateParams))).thenReturn(smsLogId);

        // 调用
        Long resultSmsLogId = smsSendService.sendSingleSms(mobile, userId, userType, templateCode, templateParams);
        // 断言
        assertEquals(smsLogId, resultSmsLogId);
        // 断言调用
        verify(smsProducer, times(0)).sendSmsSendMessage(anyLong(), anyString(),
                anyLong(), any(), anyList());
    }

    @Test
    public void testCheckSmsTemplateValid_notExists() {
        // 准备参数
        String templateCode = randomString();

        // 调用，并断言异常
        assertServiceException(() -> smsSendService.validateSmsTemplate(templateCode),
                SMS_SEND_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testBuildTemplateParams_paramMiss() {
        // 准备参数
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();

        // 调用，并断言异常
        assertServiceException(() -> smsSendService.buildTemplateParams(template, templateParams),
                SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testCheckMobile_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> smsSendService.validateMobile(null),
                SMS_SEND_MOBILE_NOT_EXISTS);
    }

    @Test
    public void testSendBatchNotify() {
        // 调用
        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> smsSendService.sendBatchSms(new ArrayList<>(), new ArrayList<>(), null, null, null)
        );
        // 断言
        assertEquals("暂时不支持该操作，感兴趣可以实现该功能哟！", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDoSendSms() throws Throwable {
        // 准备参数
        SmsSendMessage message = randomPojo(SmsSendMessage.class);
        // mock SmsClient 的方法
        SmsClient smsClient = spy(SmsClient.class);
        when(smsChannelService.getSmsClient(eq(message.getChannelId()))).thenReturn(smsClient);
        // mock SmsClient 的方法
        SmsSendRespDTO sendResult = randomPojo(SmsSendRespDTO.class);
        when(smsClient.sendSms(eq(message.getLogId()), eq(message.getMobile()), eq(message.getApiTemplateId()),
                eq(message.getTemplateParams()))).thenReturn(sendResult);

        // 调用
        smsSendService.doSendSms(message);
        // 断言
        verify(smsLogService).updateSmsSendResult(eq(message.getLogId()),
                eq(sendResult.getSuccess()), eq(sendResult.getApiCode()),
                eq(sendResult.getApiMsg()), eq(sendResult.getApiRequestId()), eq(sendResult.getSerialNo()));
    }

    @Test
    public void testDoSendSms_exception() {
        // 准备参数
        SmsSendMessage message = randomPojo(SmsSendMessage.class);
        // mock SmsClient 的方法
        SmsClient smsClient = spy(SmsClient.class);
        when(smsChannelService.getSmsClient(eq(message.getChannelId()))).thenReturn(smsClient);
        // mock SmsClient 抛异常
        try {
            when(smsClient.sendSms(eq(message.getLogId()), eq(message.getMobile()),
                    eq(message.getApiTemplateId()), eq(message.getTemplateParams())))
                    .thenThrow(new RuntimeException("send fail"));
        } catch (Throwable ignored) {
        }

        // 调用
        smsSendService.doSendSms(message);
        // 断言：异常被捕获，更新为发送失败
        verify(smsLogService).updateSmsSendResult(eq(message.getLogId()),
                eq(false), eq("EXCEPTION"), anyString(), eq(null), eq(null));
    }

    @Test
    public void testReceiveSmsStatus() throws Throwable {
        // 准备参数
        String channelCode = randomString();
        String text = randomString();
        // mock SmsClient 的方法
        SmsClient smsClient = spy(SmsClient.class);
        when(smsChannelService.getSmsClient(eq(channelCode))).thenReturn(smsClient);
        // mock SmsClient 的方法
        List<SmsReceiveRespDTO> receiveResults = randomPojoList(SmsReceiveRespDTO.class);
        when(smsClient.parseSmsReceiveStatus(eq(text))).thenReturn(receiveResults);

        // 调用
        smsSendService.receiveSmsStatus(channelCode, text);
        // 断言
        receiveResults.forEach(result -> verify(smsLogService).updateSmsReceiveResult(
                eq(result.getLogId()), eq(result.getSerialNo()), eq(result.getSuccess()),
                eq(result.getReceiveTime()), eq(result.getErrorCode()), eq(result.getErrorMsg())));
    }

}
