package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.system.dal.dataobject.SmsLogDO;
import vip.appap.suxin.module.system.dal.dataobject.SmsTemplateDO;
import vip.appap.suxin.module.system.dal.mysql.SmsLogMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.HashMap;

import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomLongId;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SmsLogServiceImpl} 的单元测试
 *
 * 覆盖日志创建状态、发送结果回写和接收结果回写
 */
public class SmsLogServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SmsLogServiceImpl smsLogService;

    @Mock
    private SmsLogMapper smsLogMapper;

    @Test
    public void testCreateSmsLog_send() {
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setContent("验证码为{code}");
            o.setParams(Lists.newArrayList("code"));
        });
        when(smsLogMapper.insert(any(SmsLogDO.class))).thenAnswer(invocation -> {
            SmsLogDO log = invocation.getArgument(0);
            log.setId(10L);
            return 1;
        });

        Long logId = smsLogService.createSmsLog("15601691300", 200L, UserTypeEnum.MEMBER.getValue(),
                true, template, "验证码为123456", new HashMap<>());

        assertEquals(10L, logId);
        // 发送状态为 INIT（待发送），接收状态为 INIT
        verify(smsLogMapper).insert(any(SmsLogDO.class));
    }

    @Test
    public void testCreateSmsLog_ignore() {
        SmsTemplateDO template = randomPojo(SmsTemplateDO.class, o -> {
            o.setContent("验证码为{code}");
            o.setParams(Lists.newArrayList("code"));
        });
        when(smsLogMapper.insert(any(SmsLogDO.class))).thenAnswer(invocation -> {
            SmsLogDO log = invocation.getArgument(0);
            log.setId(10L);
            return 1;
        });

        smsLogService.createSmsLog("15601691300", 200L, UserTypeEnum.MEMBER.getValue(),
                false, template, "验证码为123456", new HashMap<>());

        verify(smsLogMapper).insert(any(SmsLogDO.class));
    }

    @Test
    public void testUpdateSmsSendResult_success() {
        Long logId = randomLongId();

        smsLogService.updateSmsSendResult(logId, true, "Ok", "send success",
                "req-1", "serial-1");

        verify(smsLogMapper).updateById(any(SmsLogDO.class));
    }

    @Test
    public void testUpdateSmsReceiveResult_byId() {
        Long logId = randomLongId();
        LocalDateTime receiveTime = LocalDateTime.of(2026, 8, 7, 10, 0, 0);

        smsLogService.updateSmsReceiveResult(logId, "serial-1", true, receiveTime, "DELIVRD", "送达成功");

        verify(smsLogMapper).updateById(any(SmsLogDO.class));
    }

    @Test
    public void testUpdateSmsReceiveResult_bySerialNo() {
        Long logId = randomLongId();
        LocalDateTime receiveTime = LocalDateTime.of(2026, 8, 7, 10, 0, 0);
        SmsLogDO log = randomPojo(SmsLogDO.class, o -> o.setId(logId));
        when(smsLogMapper.selectByApiSerialNo("serial-1")).thenReturn(log);

        smsLogService.updateSmsReceiveResult(null, "serial-1", false, receiveTime, "FAIL", "送达失败");

        verify(smsLogMapper).selectByApiSerialNo("serial-1");
        verify(smsLogMapper).updateById(any(SmsLogDO.class));
    }

    @Test
    public void testUpdateSmsReceiveResult_bySerialNo_notFound() {
        when(smsLogMapper.selectByApiSerialNo("serial-unknown")).thenReturn(null);

        // 无法匹配回执时，不得误更新其他日志
        smsLogService.updateSmsReceiveResult(null, "serial-unknown", true,
                LocalDateTime.now(), "DELIVRD", "送达成功");

        verify(smsLogMapper).selectByApiSerialNo("serial-unknown");
        verify(smsLogMapper, never()).updateById(any(SmsLogDO.class));
    }

}

