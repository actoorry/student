package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.system.controller.admin.vo.SmsChannelSaveReqVO;
import vip.appap.suxin.module.system.dal.dataobject.SmsChannelDO;
import vip.appap.suxin.module.system.dal.mysql.SmsChannelMapper;
import vip.appap.suxin.module.system.framework.sms.core.client.SmsClient;
import vip.appap.suxin.module.system.framework.sms.core.client.SmsClientFactory;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomPojo;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CHANNEL_HAS_CHILDREN;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CHANNEL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SmsChannelServiceImpl} 的单元测试
 *
 * 覆盖渠道 CRUD、模板占用校验和客户端获取
 */
public class SmsChannelServiceTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SmsChannelServiceImpl smsChannelService;

    @Mock
    private SmsClientFactory smsClientFactory;
    @Mock
    private SmsChannelMapper smsChannelMapper;
    @Mock
    private SmsTemplateService smsTemplateService;

    @Test
    public void testCreateSmsChannel_success() {
        SmsChannelSaveReqVO reqVO = randomPojo(SmsChannelSaveReqVO.class);
        when(smsChannelMapper.insert(any(SmsChannelDO.class))).thenAnswer(invocation -> {
            SmsChannelDO channel = invocation.getArgument(0);
            channel.setId(10L);
            return 1;
        });

        Long channelId = smsChannelService.createSmsChannel(reqVO);

        assertEquals(10L, channelId);
    }

    @Test
    public void testUpdateSmsChannel_success() {
        SmsChannelSaveReqVO reqVO = randomPojo(SmsChannelSaveReqVO.class, o -> o.setId(10L));
        when(smsChannelMapper.selectById(10L)).thenReturn(randomPojo(SmsChannelDO.class, o -> o.setId(10L)));

        smsChannelService.updateSmsChannel(reqVO);

        verify(smsChannelMapper).updateById(any(SmsChannelDO.class));
    }

    @Test
    public void testUpdateSmsChannel_notExists() {
        SmsChannelSaveReqVO reqVO = randomPojo(SmsChannelSaveReqVO.class, o -> o.setId(10L));
        when(smsChannelMapper.selectById(10L)).thenReturn(null);

        assertServiceException(() -> smsChannelService.updateSmsChannel(reqVO), SMS_CHANNEL_NOT_EXISTS);
        verify(smsChannelMapper, never()).updateById(any(SmsChannelDO.class));
    }

    @Test
    public void testDeleteSmsChannel_success() {
        when(smsChannelMapper.selectById(10L)).thenReturn(randomPojo(SmsChannelDO.class, o -> o.setId(10L)));
        when(smsTemplateService.getSmsTemplateCountByChannelId(10L)).thenReturn(0L);

        smsChannelService.deleteSmsChannel(10L);

        verify(smsChannelMapper).deleteById(10L);
    }

    @Test
    public void testDeleteSmsChannel_notExists() {
        when(smsChannelMapper.selectById(10L)).thenReturn(null);

        assertServiceException(() -> smsChannelService.deleteSmsChannel(10L), SMS_CHANNEL_NOT_EXISTS);
        verify(smsChannelMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testDeleteSmsChannel_hasChildren() {
        when(smsChannelMapper.selectById(10L)).thenReturn(randomPojo(SmsChannelDO.class, o -> o.setId(10L)));
        when(smsTemplateService.getSmsTemplateCountByChannelId(10L)).thenReturn(3L);

        assertServiceException(() -> smsChannelService.deleteSmsChannel(10L), SMS_CHANNEL_HAS_CHILDREN);
        verify(smsChannelMapper, never()).deleteById(any(Long.class));
    }

    @Test
    public void testGetSmsChannel() {
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(10L));
        when(smsChannelMapper.selectById(10L)).thenReturn(channel);

        assertSame(channel, smsChannelService.getSmsChannel(10L));
    }

    @Test
    public void testGetSmsClient_id() {
        SmsChannelDO channel = randomPojo(SmsChannelDO.class, o -> o.setId(10L));
        SmsClient smsClient = org.mockito.Mockito.mock(SmsClient.class);
        when(smsChannelMapper.selectById(10L)).thenReturn(channel);
        when(smsClientFactory.createOrUpdateSmsClient(any())).thenReturn(smsClient);

        assertSame(smsClient, smsChannelService.getSmsClient(10L));
    }

    @Test
    public void testGetSmsClient_code() {
        SmsClient smsClient = org.mockito.Mockito.mock(SmsClient.class);
        when(smsClientFactory.getSmsClient(eq("TENCENT"))).thenReturn(smsClient);

        assertSame(smsClient, smsChannelService.getSmsClient("TENCENT"));
    }

}

