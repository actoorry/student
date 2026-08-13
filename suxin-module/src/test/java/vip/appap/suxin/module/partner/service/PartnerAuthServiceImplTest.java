package vip.appap.suxin.module.partner.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerWxMiniLoginReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerSmsLoginReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerLoginReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.PartnerLoginRespVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.service.PartnerMemberService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.sales.service.PartnerAuthServiceImpl;
import vip.appap.suxin.module.sales.service.SalesBrokerageUserService;
import vip.appap.suxin.module.system.api.SmsCodeApi;
import vip.appap.suxin.module.system.api.dto.SmsCodeUseReqDTO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2AccessTokenDO;
import vip.appap.suxin.module.system.dal.dataobject.SocialUserBindDO;
import vip.appap.suxin.module.system.dal.dataobject.SocialUserDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.dal.mysql.SocialUserBindMapper;
import vip.appap.suxin.module.system.dal.mysql.SocialUserMapper;
import vip.appap.suxin.module.system.service.LoginLogService;
import vip.appap.suxin.module.system.service.OAuth2TokenService;
import vip.appap.suxin.module.system.service.SocialClientService;
import vip.appap.suxin.module.system.service.SocialUserService;
import vip.appap.suxin.module.system.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.BROKERAGE_USER_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PartnerAuthServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PartnerAuthServiceImpl service;

    @Mock
    private PartnerService partnerService;
    @Mock
    private PartnerMapper partnerMapper;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OAuth2TokenService oauth2TokenService;
    @Mock
    private LoginLogService loginLogService;
    @Mock
    private SocialUserService socialUserService;
    @Mock
    private SocialClientService socialClientService;
    @Mock
    private SocialUserMapper socialUserMapper;
    @Mock
    private SocialUserBindMapper socialUserBindMapper;
    @Mock
    private PartnerMemberService partnerMemberService;
    @Mock
    private SalesBrokerageUserService brokerageUserService;
    @Mock
    private SmsCodeApi smsCodeApi;

    @BeforeEach
    void setUpCommonMocks() {
        WxMaJscode2SessionResult sessionResult = new WxMaJscode2SessionResult();
        sessionResult.setOpenid("openid-1");
        sessionResult.setSessionKey("session-key");
        lenient().when(socialClientService.getWxMaSessionInfo(anyInt(), anyString())).thenReturn(sessionResult);

        WxMaPhoneNumberInfo phoneNumberInfo = new WxMaPhoneNumberInfo();
        phoneNumberInfo.setPhoneNumber("13800138000");
        lenient().when(socialClientService.getWxMaPhoneNumberInfo(anyInt(), anyString())).thenReturn(phoneNumberInfo);

        lenient().doAnswer(invocation -> {
            SocialUserDO socialUser = invocation.getArgument(0);
            socialUser.setId(501L);
            return 1;
        }).when(socialUserMapper).insert(any(SocialUserDO.class));
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        OAuth2AccessTokenDO token = new OAuth2AccessTokenDO();
        token.setAccessToken("access-token");
        token.setRefreshToken("refresh-token");
        token.setUserId(200L);
        lenient().when(oauth2TokenService.createAccessToken(anyLong(), anyInt(), anyString(), any())).thenReturn(token);
    }

    @Test
    void weixinMiniLogin_newUserBindsInviter() {
        PartnerDO partner = partner(200L);
        when(socialUserMapper.selectByTypeAndOpenid(anyInt(), anyString())).thenReturn(null);
        when(socialUserBindMapper.selectByUserTypeAndSocialUserId(anyInt(), anyLong())).thenReturn(null);
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(null);
        when(partnerService.createPartner(any())).thenReturn(200L);
        when(partnerService.getPartner(200L)).thenReturn(partner);
        when(brokerageUserService.bindBrokerageUser(200L, 300L)).thenReturn(true);

        PartnerLoginRespVO response = service.weixinMiniLogin(loginReq(300L));

        verify(brokerageUserService).bindBrokerageUser(200L, 300L);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(200L, response.getUserId());
    }

    @Test
    void weixinMiniLogin_existingMobileUserDoesNotOverwriteInviter() {
        PartnerDO partner = partner(201L);
        when(socialUserMapper.selectByTypeAndOpenid(anyInt(), anyString())).thenReturn(null);
        when(socialUserBindMapper.selectByUserTypeAndSocialUserId(anyInt(), anyLong())).thenReturn(null);
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(partner);

        service.weixinMiniLogin(loginReq(300L));

        verify(brokerageUserService, never()).bindBrokerageUser(anyLong(), anyLong());
    }

    @Test
    void weixinMiniLogin_existingBindUserDoesNotOverwriteInviter() {
        PartnerDO partner = partner(202L);
        SocialUserBindDO bind = SocialUserBindDO.builder()
                .id(901L)
                .userId(202L)
                .socialUserId(501L)
                .build();
        when(socialUserMapper.selectByTypeAndOpenid(anyInt(), anyString())).thenReturn(null);
        when(socialUserBindMapper.selectByUserTypeAndSocialUserId(anyInt(), anyLong())).thenReturn(bind);
        when(partnerService.getPartner(202L)).thenReturn(partner);

        service.weixinMiniLogin(loginReq(300L));

        verify(brokerageUserService, never()).bindBrokerageUser(anyLong(), anyLong());
    }

    @Test
    void weixinMiniLogin_bindBrokerageUserFailsThrowsException() {
        when(socialUserMapper.selectByTypeAndOpenid(anyInt(), anyString())).thenReturn(null);
        when(socialUserBindMapper.selectByUserTypeAndSocialUserId(anyInt(), anyLong())).thenReturn(null);
        when(partnerMapper.selectByMobile("13800138000")).thenReturn(null);
        when(partnerService.createPartner(any())).thenReturn(200L);
        when(partnerService.getPartner(200L)).thenReturn(partner(200L));
        when(brokerageUserService.bindBrokerageUser(200L, 300L))
                .thenThrow(exception(BROKERAGE_USER_NOT_EXISTS));

        assertThrows(ServiceException.class, () -> service.weixinMiniLogin(loginReq(300L)));

        verify(brokerageUserService).bindBrokerageUser(200L, 300L);
    }

    @Test
    void weixinMiniLogin_blankPhoneCodeRejectsBeforeWeChatSessionLookup() {
        PartnerWxMiniLoginReqVO reqVO = loginReq(null);
        reqVO.setPhoneCode(" ");

        assertThrows(ServiceException.class, () -> service.weixinMiniLogin(reqVO));

        verify(socialClientService, never()).getWxMaSessionInfo(anyInt(), anyString());
        verify(oauth2TokenService, never()).createAccessToken(anyLong(), anyInt(), anyString(), any());
    }

    // ==================== 场景 1：客商短信登录 ====================

    @Test
    void smsLogin_existingPartnerSuccess() {
        PartnerDO partner = partner(200L);
        when(partnerService.getPartnerByMobile("13800138000")).thenReturn(partner);
        OAuth2AccessTokenDO token = new OAuth2AccessTokenDO();
        token.setAccessToken("access-token");
        token.setRefreshToken("refresh-token");
        token.setUserId(200L);
        when(oauth2TokenService.createAccessToken(anyLong(), anyInt(), anyString(), any())).thenReturn(token);

        PartnerSmsLoginReqVO reqVO = new PartnerSmsLoginReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setCode("123456");

        PartnerLoginRespVO respVO = service.smsLogin(reqVO);

        // 必须消费场景 1 验证码
        verify(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));
        assertEquals("access-token", respVO.getAccessToken());
        assertEquals(200L, respVO.getUserId());
    }

    @Test
    void smsLogin_newMobileAutoRegister() {
        when(partnerService.getPartnerByMobile("13800138000")).thenReturn(null);
        when(partnerService.createPartner(any())).thenReturn(200L);
        when(adminUserService.createUser(any())).thenReturn(200L);
        when(partnerService.getPartner(200L)).thenReturn(partner(200L));
        OAuth2AccessTokenDO token = new OAuth2AccessTokenDO();
        token.setAccessToken("access-token");
        token.setRefreshToken("refresh-token");
        token.setUserId(200L);
        when(oauth2TokenService.createAccessToken(anyLong(), anyInt(), anyString(), any())).thenReturn(token);

        PartnerSmsLoginReqVO reqVO = new PartnerSmsLoginReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setCode("123456");

        PartnerLoginRespVO respVO = service.smsLogin(reqVO);

        // 新手机号必须自动注册（共享主键 partner + system user）
        verify(partnerService).createPartner(any());
        verify(adminUserService).createUser(any());
        verify(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));
        assertEquals(200L, respVO.getUserId());
    }

    @Test
    void smsLogin_disabledPartnerRejected() {
        PartnerDO partner = partner(200L);
        partner.setStatus(CommonStatusEnum.DISABLE.getStatus());
        when(partnerService.getPartnerByMobile("13800138000")).thenReturn(partner);

        PartnerSmsLoginReqVO reqVO = new PartnerSmsLoginReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setCode("123456");

        assertThrows(ServiceException.class, () -> service.smsLogin(reqVO));
        verify(oauth2TokenService, never()).createAccessToken(anyLong(), anyInt(), anyString(), any());
    }

    @Test
    void smsLogin_wrongCodeRejected() {
        doThrow(exception(vip.appap.suxin.module.system.enums.ErrorCodeConstants.SMS_CODE_NOT_FOUND))
                .when(smsCodeApi).useSmsCode(any(SmsCodeUseReqDTO.class));

        PartnerSmsLoginReqVO reqVO = new PartnerSmsLoginReqVO();
        reqVO.setMobile("13800138000");
        reqVO.setCode("000000");

        assertThrows(ServiceException.class, () -> service.smsLogin(reqVO));
        verify(partnerService, never()).getPartnerByMobile(anyString());
        verify(oauth2TokenService, never()).createAccessToken(anyLong(), anyInt(), anyString(), any());
    }

    @Test
    void smsLogin_sharedPrimaryKeyUserPasswordUpdated() {
        // 共享主键：partner.id == system_users.id，密码登录走 system_users.password
        PartnerDO partner = partner(200L);
        AdminUserDO user = new AdminUserDO();
        user.setId(200L);
        user.setPassword("encoded-password");
        when(partnerService.getPartnerByMobile("13800138000")).thenReturn(partner);
        when(adminUserService.getUser(200L)).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        OAuth2AccessTokenDO token = new OAuth2AccessTokenDO();
        token.setAccessToken("access-token");
        token.setRefreshToken("refresh-token");
        token.setUserId(200L);
        when(oauth2TokenService.createAccessToken(anyLong(), anyInt(), anyString(), any())).thenReturn(token);

        // 密码登录（login）也走共享主键
        PartnerLoginReqVO loginReqVO = new PartnerLoginReqVO();
        loginReqVO.setMobile("13800138000");
        loginReqVO.setPassword("123456");

        PartnerLoginRespVO respVO = service.login(loginReqVO);

        verify(adminUserService).getUser(200L);
        assertEquals(200L, respVO.getUserId());
    }

    private PartnerWxMiniLoginReqVO loginReq(Long bindUserId) {
        PartnerWxMiniLoginReqVO reqVO = new PartnerWxMiniLoginReqVO();
        reqVO.setCode("code-1");
        reqVO.setPhoneCode("phone-code-1");
        reqVO.setNickname("new-user");
        reqVO.setAvatarUrl("https://example.com/avatar.png");
        reqVO.setBindUserId(bindUserId);
        return reqVO;
    }

    private PartnerDO partner(Long id) {
        PartnerDO partner = new PartnerDO();
        partner.setId(id);
        partner.setMobile("13800138000");
        partner.setNickname("partner-" + id);
        partner.setAvatar("https://example.com/avatar.png");
        partner.setStatus(CommonStatusEnum.ENABLE.getStatus());
        partner.setIsMember(true);
        return partner;
    }
}
