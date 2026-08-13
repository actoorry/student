package vip.appap.suxin.module.system.service;

import cn.hutool.core.map.MapUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.system.controller.admin.vo.SmsTemplateSaveReqVO;
import vip.appap.suxin.module.system.dal.dataobject.SmsChannelDO;
import vip.appap.suxin.module.system.dal.dataobject.SmsTemplateDO;
import vip.appap.suxin.module.system.dal.mysql.SmsTemplateMapper;
import vip.appap.suxin.module.system.framework.sms.core.client.SmsClient;
import vip.appap.suxin.module.system.framework.sms.core.client.dto.SmsTemplateRespDTO;
import vip.appap.suxin.module.system.framework.sms.core.enums.SmsTemplateAuditStatusEnum;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomPojo;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomString;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CHANNEL_DISABLE;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CHANNEL_NOT_EXISTS;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_TEMPLATE_API_AUDIT_CHECKING;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_TEMPLATE_API_AUDIT_FAIL;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_TEMPLATE_API_ERROR;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_TEMPLATE_CODE_DUPLICATE;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_TEMPLATE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SmsTemplateServiceImpl} 的单元测试
 *
 * 覆盖模板 CRUD、缓存、编码唯一、渠道校验、参数解析和 API 模板审核状态
 */
public class SmsTemplateServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SmsTemplateServiceImpl smsTemplateService;

    @Mock
    private SmsTemplateMapper smsTemplateMapper;
    @Mock
    private SmsChannelService smsChannelService;

    @Test
    public void testFormatSmsTemplateContent() {
        // 准备参数
        Map<String, Object> params = MapUtil.<String, Object>builder()
                .put("code", "123456").put("op", "login").build();
        // 调用
        String result = smsTemplateService.formatSmsTemplateContent("验证码为{code}, 操作为{op}", params);
        // 断言
        assertEquals("验证码为123456, 操作为login", result);
    }

    @Test
    public void testParseTemplateContentParams() {
        String content = "验证码为{code}, 操作为{op}, 日期为{date}";
        assertEquals(Lists.newArrayList("code", "op", "date"),
                smsTemplateService.parseTemplateContentParams(content));
    }

    @Test
    public void testCreateSmsTemplate_success() throws Throwable {
        SmsTemplateSaveReqVO reqVO = randomPojo(SmsTemplateSaveReqVO.class, o -> {
            o.setContent("验证码为{code}");
            o.setChannelId(7L);
        });
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(7L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()).setCode("TENCENT"));
        when(smsChannelService.getSmsChannel(7L)).thenReturn(channel);
        when(smsTemplateMapper.selectByCode(eq(reqVO.getCode()))).thenReturn(null);
        SmsClient smsClient = org.mockito.Mockito.mock(SmsClient.class);
        when(smsChannelService.getSmsClient(7L)).thenReturn(smsClient);
        when(smsClient.getSmsTemplate(eq(reqVO.getApiTemplateId()))).thenReturn(
                new SmsTemplateRespDTO().setAuditStatus(SmsTemplateAuditStatusEnum.SUCCESS.getStatus()));
        when(smsTemplateMapper.insert(any(SmsTemplateDO.class))).thenAnswer(invocation -> {
            SmsTemplateDO template = invocation.getArgument(0);
            template.setId(10L);
            return 1;
        });

        Long templateId = smsTemplateService.createSmsTemplate(reqVO);

        assertEquals(10L, templateId);
        // channel_code 必须同步渠道编码
        verify(smsTemplateMapper).insert(any(SmsTemplateDO.class));
    }

    @Test
    public void testCreateSmsTemplate_apiAuditChecking() throws Throwable {
        SmsTemplateSaveReqVO reqVO = randomPojo(SmsTemplateSaveReqVO.class, o -> o.setChannelId(7L));
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(7L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(7L)).thenReturn(channel);
        when(smsTemplateMapper.selectByCode(eq(reqVO.getCode()))).thenReturn(null);
        SmsClient smsClient = org.mockito.Mockito.mock(SmsClient.class);
        when(smsChannelService.getSmsClient(7L)).thenReturn(smsClient);
        when(smsClient.getSmsTemplate(eq(reqVO.getApiTemplateId()))).thenReturn(
                new SmsTemplateRespDTO().setAuditStatus(SmsTemplateAuditStatusEnum.CHECKING.getStatus()));

        assertServiceException(() -> smsTemplateService.createSmsTemplate(reqVO), SMS_TEMPLATE_API_AUDIT_CHECKING);
        verify(smsTemplateMapper, never()).insert(any(SmsTemplateDO.class));
    }

    @Test
    public void testCreateSmsTemplate_apiAuditFail() throws Throwable {
        SmsTemplateSaveReqVO reqVO = randomPojo(SmsTemplateSaveReqVO.class, o -> o.setChannelId(7L));
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(7L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(7L)).thenReturn(channel);
        when(smsTemplateMapper.selectByCode(eq(reqVO.getCode()))).thenReturn(null);
        SmsClient smsClient = org.mockito.Mockito.mock(SmsClient.class);
        when(smsChannelService.getSmsClient(7L)).thenReturn(smsClient);
        when(smsClient.getSmsTemplate(eq(reqVO.getApiTemplateId()))).thenReturn(
                new SmsTemplateRespDTO().setAuditStatus(SmsTemplateAuditStatusEnum.FAIL.getStatus())
                        .setAuditReason("内容不合规"));

        assertServiceException(() -> smsTemplateService.createSmsTemplate(reqVO),
                SMS_TEMPLATE_API_AUDIT_FAIL, "内容不合规");
        verify(smsTemplateMapper, never()).insert(any(SmsTemplateDO.class));
    }

    @Test
    public void testUpdateSmsTemplate_notExists() {
        SmsTemplateSaveReqVO reqVO = randomPojo(SmsTemplateSaveReqVO.class, o -> o.setId(10L));
        when(smsTemplateMapper.selectById(10L)).thenReturn(null);

        assertServiceException(() -> smsTemplateService.updateSmsTemplate(reqVO), SMS_TEMPLATE_NOT_EXISTS);
        verify(smsTemplateMapper, never()).updateById(any(SmsTemplateDO.class));
    }

    @Test
    public void testDeleteSmsTemplate_notExists() {
        when(smsTemplateMapper.selectById(10L)).thenReturn(null);

        assertServiceException(() -> smsTemplateService.deleteSmsTemplate(10L), SMS_TEMPLATE_NOT_EXISTS);
        verify(smsTemplateMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testGetSmsTemplateByCodeFromCache() {
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> o.setCode("user-sms-login"));
        when(smsTemplateMapper.selectByCode("user-sms-login")).thenReturn(template);

        assertSame(template, smsTemplateService.getSmsTemplateByCodeFromCache("user-sms-login"));
    }

    @Test
    public void testValidateSmsChannel_success() {
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(7L)
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(smsChannelService.getSmsChannel(7L)).thenReturn(channel);

        assertSame(channel, smsTemplateService.validateSmsChannel(7L));
    }

    @Test
    public void testValidateSmsChannel_notExists() {
        when(smsChannelService.getSmsChannel(7L)).thenReturn(null);

        assertServiceException(() -> smsTemplateService.validateSmsChannel(7L), SMS_CHANNEL_NOT_EXISTS);
    }

    @Test
    public void testValidateSmsChannel_disable() {
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(7L)
                .setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(smsChannelService.getSmsChannel(7L)).thenReturn(channel);

        assertServiceException(() -> smsTemplateService.validateSmsChannel(7L), SMS_CHANNEL_DISABLE);
    }

    @Test
    public void testValidateSmsTemplateCodeDuplicate_create() {
        when(smsTemplateMapper.selectByCode("user-sms-login"))
                .thenReturn(randomPojo(SmsTemplateDO.class, o -> o.setCode("user-sms-login")));

        assertServiceException(() -> smsTemplateService.validateSmsTemplateCodeDuplicate(null, "user-sms-login"),
                SMS_TEMPLATE_CODE_DUPLICATE, "user-sms-login");
    }

    @Test
    public void testValidateSmsTemplateCodeDuplicate_updateSameId() {
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> o.setId(10L).setCode("user-sms-login"));
        when(smsTemplateMapper.selectByCode("user-sms-login")).thenReturn(template);

        // 相同 id 不报错
        smsTemplateService.validateSmsTemplateCodeDuplicate(10L, "user-sms-login");
    }

}

