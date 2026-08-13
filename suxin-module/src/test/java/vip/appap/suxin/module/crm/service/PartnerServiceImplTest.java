package vip.appap.suxin.module.crm.service;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.system.api.SmsCodeApi;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import vip.appap.suxin.module.system.service.AdminUserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.USER_MOBILE_NOT_EXISTS;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.USER_MOBILE_USED;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CODE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PartnerServiceImpl} 场景 2/3/4 账户业务单元测试
 *
 * 覆盖换绑手机号、短信修改密码、短信重置密码的成功、错误场景、重复消费、
 * 重复手机号、未知手机号和验证码消费（事务回滚依赖 @Transactional，由集成测试验证）
 */
class PartnerServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerServiceImpl service;

    @Mock
    private PartnerMapper partnerMapper;
    @Mock
    private SmsCodeApi smsCodeApi;
    @Mock
    private AdminUserService adminUserService;

    private PartnerDO partner(Long id, String mobile) {
        PartnerDO partner = new PartnerDO();
        partner.setId(id);
        partner.setMobile(mobile);
        partner.setIsMember(true);
        partner.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return partner;
    }

    // ==================== 场景 2：换绑手机号 ====================

    @Test
    void updatePartnerMobile_success() {
        PartnerDO partner = partner(200L, "13800138000");
        when(partnerMapper.selectById(200L)).thenReturn(partner);
        when(partnerMapper.selectByMobile("13900139000")).thenReturn(null);

        service.updatePartnerMobile(200L, "13900139000", "123456");

        // 消费目标手机号场景 2 验证码
        ArgumentCaptor<SmsCodeUseReqDTO> useCaptor = ArgumentCaptor.forClass(SmsCodeUseReqDTO.class);
        verify(smsCodeApi).useSmsCode(useCaptor.capture());
        assertEquals("13900139000", useCaptor.getValue().getMobile());
        assertEquals("123456", useCaptor.getValue().getCode());
        assertEquals(SmsSceneEnum.MEMBER_UPDATE_MOBILE.getScene(), useCaptor.getValue().getScene());
        // 更新 partner 手机号
        ArgumentCaptor<PartnerDO> updateCaptor = ArgumentCaptor.forClass(PartnerDO.class);
        verify(partnerMapper).updateById(updateCaptor.capture());
        assertEquals(200L, updateCaptor.getValue().getId());
        assertEquals("13900139000", updateCaptor.getValue().getMobile());
        // 同步共享主键 system user 用户名（username == 旧手机号时）
        verify(adminUserService).updateUserUsernameIfMobile(200L, "13800138000", "13900139000");
    }

    @Test
    void updatePartnerMobile_partnerNotExists() {
        when(partnerMapper.selectById(200L)).thenReturn(null);

        assertServiceException(() -> service.updatePartnerMobile(200L, "13900139000", "123456"),
                USER_NOT_EXISTS);
        verify(smsCodeApi, never()).useSmsCode(any());
    }

    @Test
    void updatePartnerMobile_mobileUsed() {
        PartnerDO partner = partner(200L, "13800138000");
        when(partnerMapper.selectById(200L)).thenReturn(partner);
        when(partnerMapper.selectByMobile("13900139000")).thenReturn(partner(300L, "13900139000"));

        assertServiceException(() -> service.updatePartnerMobile(200L, "13900139000", "123456"),
                USER_MOBILE_USED, "13900139000");
        // 目标手机号被占用时，不得消费验证码
        verify(smsCodeApi, never()).useSmsCode(any());
        verify(partnerMapper, never()).updateById(any(PartnerDO.class));
    }

    @Test
    void updatePartnerMobile_codeRejected() {
        PartnerDO partner = partner(200L, "13800138000");
        when(partnerMapper.selectById(200L)).thenReturn(partner);
        when(partnerMapper.selectByMobile("13900139000")).thenReturn(null);
        doThrow(exception(SMS_CODE_NOT_FOUND)).when(smsCodeApi).useSmsCode(any());

        assertServiceException(() -> service.updatePartnerMobile(200L, "13900139000", "000000"),
                SMS_CODE_NOT_FOUND);
        verify(partnerMapper, never()).updateById(any(PartnerDO.class));
    }

    // ==================== 场景 3：短信修改密码 ====================

    @Test
    void updatePartnerPassword_success() {
        PartnerDO partner = partner(200L, "13800138000");
        when(partnerMapper.selectById(200L)).thenReturn(partner);

        service.updatePartnerPassword(200L, "123456", "654321");

        // 消费当前手机号场景 3 验证码
        ArgumentCaptor<SmsCodeUseReqDTO> useCaptor = ArgumentCaptor.forClass(SmsCodeUseReqDTO.class);
        verify(smsCodeApi).useSmsCode(useCaptor.capture());
        assertEquals("13800138000", useCaptor.getValue().getMobile());
        assertEquals("123456", useCaptor.getValue().getCode());
        assertEquals(SmsSceneEnum.MEMBER_UPDATE_PASSWORD.getScene(), useCaptor.getValue().getScene());
        // 更新共享主键 system user 密码
        verify(adminUserService).updateUserPassword(200L, "654321");
    }

    @Test
    void updatePartnerPassword_partnerNotExists() {
        when(partnerMapper.selectById(200L)).thenReturn(null);

        assertServiceException(() -> service.updatePartnerPassword(200L, "123456", "654321"),
                USER_NOT_EXISTS);
        verify(smsCodeApi, never()).useSmsCode(any());
        verify(adminUserService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void updatePartnerPassword_codeRejected() {
        when(partnerMapper.selectById(200L)).thenReturn(partner(200L, "13800138000"));
        doThrow(exception(SMS_CODE_NOT_FOUND)).when(smsCodeApi).useSmsCode(any());

        assertServiceException(() -> service.updatePartnerPassword(200L, "000000", "654321"),
                SMS_CODE_NOT_FOUND);
        // 验证码未通过时，不得更新密码
        verify(adminUserService, never()).updateUserPassword(anyLong(), anyString());
    }

    // ==================== 场景 4：忘记密码 ====================

    @Test
    void resetPartnerPassword_success() {
        PartnerDO partner = partner(200L, "13800138000");
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(partner);

        service.resetPartnerPassword("13800138000", "123456", "654321");

        // 消费手机号场景 4 验证码
        ArgumentCaptor<SmsCodeUseReqDTO> useCaptor = ArgumentCaptor.forClass(SmsCodeUseReqDTO.class);
        verify(smsCodeApi).useSmsCode(useCaptor.capture());
        assertEquals("13800138000", useCaptor.getValue().getMobile());
        assertEquals("123456", useCaptor.getValue().getCode());
        assertEquals(SmsSceneEnum.MEMBER_RESET_PASSWORD.getScene(), useCaptor.getValue().getScene());
        // 更新共享主键 system user 密码
        verify(adminUserService).updateUserPassword(200L, "654321");
    }

    @Test
    void resetPartnerPassword_mobileNotExists() {
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(null);

        assertServiceException(() -> service.resetPartnerPassword("13800138000", "123456", "654321"),
                USER_MOBILE_NOT_EXISTS);
        verify(smsCodeApi, never()).useSmsCode(any());
        verify(adminUserService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void resetPartnerPassword_codeRejected() {
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(partner(200L, "13800138000"));
        doThrow(exception(SMS_CODE_NOT_FOUND)).when(smsCodeApi).useSmsCode(any());

        assertServiceException(() -> service.resetPartnerPassword("13800138000", "000000", "654321"),
                SMS_CODE_NOT_FOUND);
        verify(adminUserService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void resetPartnerPassword_businessUpdateFailurePropagates() {
        // 业务更新失败（例如密码策略异常）时，异常必须向上传播，@Transactional 保证验证码消费一并回滚
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(partner(200L, "13800138000"));
        doThrow(new RuntimeException("password policy error")).when(adminUserService).updateUserPassword(200L, "bad");

        assertThrows(RuntimeException.class,
                () -> service.resetPartnerPassword("13800138000", "123456", "bad"));
    }

}
