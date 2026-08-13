package vip.appap.suxin.module.system.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.system.api.SmsCodeApi;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.controller.admin.vo.AuthLoginRespVO;
import vip.appap.suxin.module.system.controller.admin.vo.AuthResetPasswordReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.AuthSmsLoginReqVO;
import vip.appap.suxin.module.system.controller.admin.vo.AuthSmsSendReqVO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2AccessTokenDO;
import vip.appap.suxin.module.system.enums.LoginLogTypeEnum;
import vip.appap.suxin.module.system.enums.LoginResultEnum;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import vip.appap.suxin.module.system.service.partner.PartnerUserService;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.service.CaptchaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.validation.Validator;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.test.core.util.AssertUtils.assertServiceException;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomLongId;
import static vip.appap.suxin.framework.test.core.util.RandomUtils.randomPojo;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.AUTH_LOGIN_USER_DISABLED;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.AUTH_MOBILE_NOT_EXISTS;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CODE_EXPIRED;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CODE_USED;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.USER_MOBILE_NOT_EXISTS;
import static vip.appap.suxin.module.system.enums.ErrorCodeConstants.USER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminAuthServiceImpl} 的单元测试
 *
 * 覆盖后台场景 21 短信登录与场景 23 短信找回密码
 */
class AdminAuthServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AdminAuthServiceImpl service;

    @Mock
    private AdminUserService userService;
    @Mock
    private LoginLogService loginLogService;
    @Mock
    private OAuth2TokenService oauth2TokenService;
    @Mock
    private SocialUserService socialUserService;
    @Mock
    private PartnerUserService partnerUserService;
    @Mock
    private Validator validator;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private SmsCodeApi smsCodeApi;

    @BeforeEach
    void setUp() {
        service.setCaptchaEnable(false); // 单元测试关闭图形验证码
    }

    // ==================== 场景 21：短信登录 ====================

    @Test
    void sendSmsCode_scene21_success() {
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername("15601691300"));
        when(userService.getUserByMobile("15601691300")).thenReturn(user);

        AuthSmsSendReqVO reqVO = AuthSmsSendReqVO.builder()
                .mobile("15601691300").scene(SmsSceneEnum.ADMIN_MEMBER_LOGIN.getScene()).build();
        service.sendSmsCode(reqVO);

        verify(smsCodeApi).sendSmsCode(any());
    }

    @Test
    void sendSmsCode_scene21_mobileNotExists() {
        when(userService.getUserByMobile("15601691300")).thenReturn(null);

        AuthSmsSendReqVO reqVO = AuthSmsSendReqVO.builder()
                .mobile("15601691300").scene(SmsSceneEnum.ADMIN_MEMBER_LOGIN.getScene()).build();

        assertServiceException(() -> service.sendSmsCode(reqVO), AUTH_MOBILE_NOT_EXISTS);
        verify(smsCodeApi, never()).sendSmsCode(any());
    }

    @Test
    void smsLogin_scene21_success() {
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setId(100L).setUsername("15601691300"));
        when(userService.getUserByMobile("15601691300")).thenReturn(user);
        OAuth2AccessTokenDO token = randomPojo(OAuth2AccessTokenDO.class, o -> {
            o.setUserId(100L).setAccessToken("access-token").setRefreshToken("refresh-token");
        });
        when(oauth2TokenService.createAccessToken(anyLong(), anyInt(), anyString(), any())).thenReturn(token);

        AuthSmsLoginReqVO reqVO = AuthSmsLoginReqVO.builder()
                .mobile("15601691300").code("123456").build();
        AuthLoginRespVO respVO = service.smsLogin(reqVO);

        // 必须消费场景 21 验证码
        verify(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));
        assertEquals("access-token", respVO.getAccessToken());
    }

    @Test
    void smsLogin_scene21_userNotExists() {
        when(userService.getUserByMobile("15601691300")).thenReturn(null);

        AuthSmsLoginReqVO reqVO = AuthSmsLoginReqVO.builder()
                .mobile("15601691300").code("123456").build();

        assertServiceException(() -> service.smsLogin(reqVO), USER_NOT_EXISTS);
        verify(oauth2TokenService, never()).createAccessToken(anyLong(), anyInt(), anyString(), any());
    }

    @Test
    void smsLogin_scene21_wrongCodeRejected() {
        // 错误验证码：useSmsCode 抛异常，不得签发令牌
        org.mockito.Mockito.doThrow(exception(SMS_CODE_USED))
                .when(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));

        AuthSmsLoginReqVO reqVO = AuthSmsLoginReqVO.builder()
                .mobile("15601691300").code("000000").build();

        assertServiceException(() -> service.smsLogin(reqVO), SMS_CODE_USED);
        verify(oauth2TokenService, never()).createAccessToken(anyLong(), anyInt(), anyString(), any());
    }

    // ==================== 场景 23：短信找回密码 ====================

    @Test
    void resetPassword_scene23_success() {
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setId(100L).setUsername("15601691300"));
        when(userService.getUserByMobile("15601691300")).thenReturn(user);

        AuthResetPasswordReqVO reqVO = AuthResetPasswordReqVO.builder()
                .mobile("15601691300").code("123456").password("654321").build();
        service.resetPassword(reqVO);

        // 必须消费场景 23 验证码并更新密码
        verify(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));
        verify(userService).updateUserPassword(100L, "654321");
    }

    @Test
    void resetPassword_scene23_mobileNotExists() {
        when(userService.getUserByMobile("15601691300")).thenReturn(null);

        AuthResetPasswordReqVO reqVO = AuthResetPasswordReqVO.builder()
                .mobile("15601691300").code("123456").password("654321").build();

        assertServiceException(() -> service.resetPassword(reqVO), USER_MOBILE_NOT_EXISTS);
        verify(smsCodeApi, never()).useSmsCode(any());
        verify(userService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void resetPassword_scene23_expiredCodeRejected() {
        when(userService.getUserByMobile("15601691300"))
                .thenReturn(randomPojo(AdminUserDO.class, o -> o.setId(100L)));
        org.mockito.Mockito.doThrow(exception(SMS_CODE_EXPIRED))
                .when(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));

        AuthResetPasswordReqVO reqVO = AuthResetPasswordReqVO.builder()
                .mobile("15601691300").code("123456").password("654321").build();

        assertServiceException(() -> service.resetPassword(reqVO), SMS_CODE_EXPIRED);
        verify(userService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void resetPassword_scene23_usedCodeRejected() {
        when(userService.getUserByMobile("15601691300"))
                .thenReturn(randomPojo(AdminUserDO.class, o -> o.setId(100L)));
        org.mockito.Mockito.doThrow(exception(SMS_CODE_USED))
                .when(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));

        AuthResetPasswordReqVO reqVO = AuthResetPasswordReqVO.builder()
                .mobile("15601691300").code("123456").password("654321").build();

        assertServiceException(() -> service.resetPassword(reqVO), SMS_CODE_USED);
        verify(userService, never()).updateUserPassword(anyLong(), anyString());
    }

    @Test
    void sendSmsCode_scene23_captchaRequired() {
        service.setCaptchaEnable(true);
        when(captchaService.verification(any())).thenReturn(ResponseModel.errorMsg("就是不对"));

        AuthSmsSendReqVO reqVO = AuthSmsSendReqVO.builder()
                .mobile("15601691300").scene(SmsSceneEnum.ADMIN_MEMBER_RESET_PASSWORD.getScene()).build();
        reqVO.setCaptchaVerification("invalid");

        // 图形验证码校验失败时，场景 23 不得发送
        assertThrows(ServiceException.class, () -> service.sendSmsCode(reqVO));
        verify(smsCodeApi, never()).sendSmsCode(any());
    }

}


