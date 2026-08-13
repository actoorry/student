package vip.appap.suxin.module.sales.service;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.util.monitor.TracerUtils;
import vip.appap.suxin.framework.common.util.servlet.ServletUtils;
import vip.appap.suxin.framework.web.core.util.WebFrameworkUtils;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerCreateReqVO;
import vip.appap.suxin.module.partner.controller.app.vo.*;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import vip.appap.suxin.module.partner.service.PartnerMemberService;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.api.SmsCodeApi;
import vip.appap.suxin.module.system.api.dto.*;
import vip.appap.suxin.module.system.controller.admin.vo.UserSaveReqVO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.dal.dataobject.OAuth2AccessTokenDO;
import vip.appap.suxin.module.system.dal.dataobject.SocialUserBindDO;
import vip.appap.suxin.module.system.dal.dataobject.SocialUserDO;
import vip.appap.suxin.module.system.dal.mysql.SocialUserBindMapper;
import vip.appap.suxin.module.system.dal.mysql.SocialUserMapper;
import vip.appap.suxin.module.system.enums.LoginLogTypeEnum;
import vip.appap.suxin.module.system.enums.LoginResultEnum;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import vip.appap.suxin.module.system.service.*;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.json.JsonUtils.toJsonString;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getTerminal;
import static vip.appap.suxin.module.partner.enums.ErrorCodeConstants.*;

/**
 * 合作伙伴认证 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
@Slf4j
public class PartnerAuthServiceImpl implements PartnerAuthService {

    /**
     * 婚恋小程序 OAuth2 客户端ID
     */
    private static final String OAUTH2_CLIENT_ID_MARRIAGE = "marriage";

    @Resource
    private PartnerService partnerService;
    @Resource
    private PartnerMapper partnerMapper;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private LoginLogService loginLogService;
    @Resource
    private SmsCodeApi smsCodeApi;
    @Resource
    private SocialUserService socialUserService;
    @Resource
    private SocialClientService socialClientService;
    @Resource
    private SocialUserMapper socialUserMapper;
    @Resource
    private SocialUserBindMapper socialUserBindMapper;
    @Resource
    private PartnerMemberService partnerMemberService;
    @Resource
    private SalesBrokerageUserService brokerageUserService;

    @Override
    public PartnerLoginRespVO login(PartnerLoginReqVO reqVO) {
        // 使用手机 + 密码，进行登录
        PartnerDO partner = login0(reqVO.getMobile(), reqVO.getPassword());

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(partner, LoginLogTypeEnum.LOGIN_MOBILE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(PartnerRegisterReqVO registerReqVO) {
        String mobile = registerReqVO.getMobile();
        String password = registerReqVO.getPassword();
        String nickname = registerReqVO.getNickname();

        // 1. 校验手机号唯一
        PartnerDO existingPartner = partnerService.getPartnerByMobile(mobile);
        if (existingPartner != null) {
            throw exception(PARTNER_MOBILE_EXISTS);
        }

        // 2. 创建 partner 记录
        PartnerCreateReqVO partnerReqVO = new PartnerCreateReqVO();
        partnerReqVO.setMobile(mobile);
        partnerReqVO.setNickname(StrUtil.isBlank(nickname) ? "用户" + mobile.substring(mobile.length() - 4) : nickname);
        partnerReqVO.setStatus((byte) CommonStatusEnum.ENABLE.getStatus().intValue());
        partnerReqVO.setIsMember(true);
        Long partnerId = partnerService.createPartner(partnerReqVO);

        // 3. 创建 system_users 记录（使用手机号作为用户名）
        UserSaveReqVO userReqVO = new UserSaveReqVO();
        userReqVO.setPartnerId(partnerId);
        userReqVO.setUsername(mobile);
        userReqVO.setPassword(passwordEncoder.encode(password));
        adminUserService.createUser(userReqVO);

        // 4. 创建 member 记录
        partnerService.initPartnerMember(partnerId, getClientIP(), getTerminal());

        return partnerId;
    }

    @Override
    public void logout(String token) {
        // 删除访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(token);
        if (accessTokenDO == null) {
            return;
        }
        // 删除成功，则记录登出日志
        createLogoutLog(accessTokenDO.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerLoginRespVO smsLogin(PartnerSmsLoginReqVO reqVO) {
        // 校验验证码
        String userIp = getClientIP();
        SmsCodeUseReqDTO useReqDTO = new SmsCodeUseReqDTO();
        useReqDTO.setMobile(reqVO.getMobile());
        useReqDTO.setCode(reqVO.getCode());
        useReqDTO.setScene(SmsSceneEnum.MEMBER_LOGIN.getScene());
        useReqDTO.setUsedIp(userIp);
        smsCodeApi.useSmsCode(useReqDTO);

        // 获得或注册用户
        PartnerDO partner = partnerService.getPartnerByMobile(reqVO.getMobile());
        if (partner == null) {
            // 自动注册
            PartnerCreateReqVO partnerReqVO = new PartnerCreateReqVO();
            partnerReqVO.setMobile(reqVO.getMobile());
            partnerReqVO.setNickname("用户" + reqVO.getMobile().substring(reqVO.getMobile().length() - 4));
            partnerReqVO.setStatus((byte) CommonStatusEnum.ENABLE.getStatus().intValue());
            partnerReqVO.setIsMember(true);
            Long partnerId = partnerService.createPartner(partnerReqVO);

            UserSaveReqVO userReqVO = new UserSaveReqVO();
            userReqVO.setPartnerId(partnerId);
            userReqVO.setUsername(reqVO.getMobile());
            userReqVO.setPassword(passwordEncoder.encode("123456")); // 默认密码
            adminUserService.createUser(userReqVO);

            partnerService.initPartnerMember(partnerId, userIp, getTerminal());
            partner = partnerService.getPartner(partnerId);
        }

        // 校验是否禁用
        if (CommonStatusEnum.isDisable(partner.getStatus())) {
            createLoginLog(partner.getId(), reqVO.getMobile(), LoginLogTypeEnum.LOGIN_SMS, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(partner, LoginLogTypeEnum.LOGIN_SMS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerLoginRespVO socialLogin(PartnerSocialLoginReqVO reqVO) {
        // 使用 code 授权码，进行登录。然后，获得到绑定的用户编号
        SocialUserRespDTO socialUser = socialUserService.getSocialUserByCode(
                UserTypeEnum.MEMBER.getValue(), reqVO.getType(), reqVO.getCode(), reqVO.getState());
        if (socialUser == null) {
            throw exception(AUTH_SOCIAL_USER_NOT_FOUND);
        }

        // 情况一：已绑定，直接读取用户信息
        PartnerDO partner;
        if (socialUser.getUserId() != null) {
            partner = partnerService.getPartner(socialUser.getUserId());
        // 情况二：未绑定，注册用户 + 绑定用户
        } else {
            // 创建 Partner
            PartnerCreateReqVO partnerReqVO = new PartnerCreateReqVO();
            partnerReqVO.setNickname(socialUser.getNickname());
            partnerReqVO.setAvatar(socialUser.getAvatar());
            partnerReqVO.setStatus((byte) CommonStatusEnum.ENABLE.getStatus().intValue());
            partnerReqVO.setIsMember(true);
            Long partnerId = partnerService.createPartner(partnerReqVO);

            // 创建 AdminUser
            UserSaveReqVO userReqVO = new UserSaveReqVO();
            userReqVO.setPartnerId(partnerId);
            userReqVO.setUsername("social" + System.currentTimeMillis());
            userReqVO.setPassword(passwordEncoder.encode("123456"));
            adminUserService.createUser(userReqVO);

            // 初始化会员信息
            partnerService.initPartnerMember(partnerId, getClientIP(), getTerminal());

            // 绑定社交用户
            socialUserService.bindSocialUser(new SocialUserBindReqDTO(partnerId, UserTypeEnum.MEMBER.getValue(),
                    reqVO.getType(), reqVO.getCode(), reqVO.getState()));

            partner = partnerService.getPartner(partnerId);
        }
        if (partner == null) {
            throw exception(AUTH_SOCIAL_USER_NOT_FOUND);
        }

        // 创建 Token 令牌，记录登录日志
        return createTokenAfterLoginSuccess(partner, LoginLogTypeEnum.LOGIN_SOCIAL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PartnerLoginRespVO weixinMiniLogin(PartnerWxMiniLoginReqVO reqVO) {
        log.info("[weixinMiniLogin][微信小程序登录开始，code={}]", reqVO.getCode());

        // 1. 校验 phoneCode 必填
        if (StrUtil.isBlank(reqVO.getPhoneCode())) {
            log.warn("[weixinMiniLogin][手机号授权码为空，code={}]", reqVO.getCode());
            throw exception(AUTH_WX_LOGIN_FAIL, "手机号授权码不能为空");
        }

        // 2. 调用微信接口获取 openid
        WxMaJscode2SessionResult sessionResult;
        try {
            sessionResult = socialClientService.getWxMaSessionInfo(UserTypeEnum.MEMBER.getValue(), reqVO.getCode());
        } catch (Exception e) {
            log.error("[weixinMiniLogin][获取微信session信息失败，code={}]", reqVO.getCode(), e);
            throw exception(AUTH_WX_LOGIN_FAIL);
        }

        // 3. 获取手机号（严格模式：失败则拒绝登录）
        String mobile;
        try {
            WxMaPhoneNumberInfo phoneInfo = socialClientService.getWxMaPhoneNumberInfo(
                    UserTypeEnum.MEMBER.getValue(), reqVO.getPhoneCode());
            mobile = phoneInfo.getPhoneNumber();
            log.info("[weixinMiniLogin][获取手机号成功，mobile={}]", mobile);
        } catch (Exception e) {
            log.error("[weixinMiniLogin][获取手机号失败，phoneCode={}]", reqVO.getPhoneCode(), e);
            throw exception(AUTH_WX_LOGIN_FAIL, "获取手机号失败，请重试");
        }
        if (StrUtil.isBlank(mobile)) {
            log.warn("[weixinMiniLogin][获取手机号为空，phoneCode={}]", reqVO.getPhoneCode());
            throw exception(AUTH_WX_LOGIN_FAIL, "获取手机号失败，请重试");
        }

        // 3. 创建或更新社交用户
        String openid = sessionResult.getOpenid();
        SocialUserDO socialUser = socialUserMapper.selectByTypeAndOpenid(
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), openid);
        // 昵称和头像：优先使用前端传入的，其次用社交用户已有的，新用户才用默认值
        String nickname = StrUtil.isNotBlank(reqVO.getNickname()) ? reqVO.getNickname() :
                (socialUser != null ? socialUser.getNickname() : null);
        String avatarUrl = StrUtil.isNotBlank(reqVO.getAvatarUrl()) ? reqVO.getAvatarUrl() :
                (socialUser != null ? socialUser.getAvatar() : null);

        // 新用户且无昵称时，使用默认昵称
        if (nickname == null && socialUser == null) {
            nickname = "微信用户";
        }
        String rawUserInfo = toJsonString(new WxMiniRawUserInfo(nickname, avatarUrl));
        if (socialUser == null) {
            socialUser = SocialUserDO.builder()
                    .type(SocialTypeEnum.WECHAT_MINI_PROGRAM.getType())
                    .openid(openid)
                    .token(sessionResult.getSessionKey())
                    .rawTokenInfo(toJsonString(sessionResult))
                    .rawUserInfo(rawUserInfo)
                    .code(reqVO.getCode())
                    .state(reqVO.getCode())
                    .nickname(nickname)
                    .avatar(avatarUrl)
                    .build();
            socialUserMapper.insert(socialUser);
        } else {
            socialUser.setToken(sessionResult.getSessionKey());
            socialUser.setRawTokenInfo(toJsonString(sessionResult));
            socialUser.setRawUserInfo(rawUserInfo);
            socialUser.setCode(reqVO.getCode());
            socialUser.setState(reqVO.getCode());
            socialUser.setNickname(nickname);
            socialUser.setAvatar(avatarUrl);
            socialUserMapper.updateById(socialUser);
        }

        // 4. 查询绑定关系
        SocialUserBindDO bind = socialUserBindMapper.selectByUserTypeAndSocialUserId(
                UserTypeEnum.MEMBER.getValue(), socialUser.getId());

        // 先尝试通过手机号查找已有 partner（手机号已注册的用户直接绑定）
        PartnerDO existingPartnerByMobile = partnerMapper.selectByMobile(mobile);

        PartnerDO partner;
        if (bind == null) {
            if (existingPartnerByMobile != null) {
                // 已注册用户：忽略分享/扫码携带的 bindUserId，直接登录
                log.info("[weixinMiniLogin][手机号已存在，直接登录，mobile={}, partnerId={}]", mobile, existingPartnerByMobile.getId());
                partner = existingPartnerByMobile;
            } else {
                // 新用户：自动注册
                log.info("[weixinMiniLogin][新用户注册，openid={}]", openid);
                partner = createWxMiniMember(socialUser, mobile);
                bindInviterIfPresent(partner.getId(), reqVO.getBindUserId());
            }
            socialUserBindMapper.insert(SocialUserBindDO.builder()
                    .userId(partner.getId())
                    .userType(UserTypeEnum.MEMBER.getValue())
                    .socialUserId(socialUser.getId())
                    .socialType(SocialTypeEnum.WECHAT_MINI_PROGRAM.getType())
                    .build());
        } else {
            // 老用户：直接登录，忽略 bindUserId
            partner = partnerService.getPartner(bind.getUserId());
            if (partner == null) {
                if (existingPartnerByMobile != null) {
                    // 绑定记录存在但用户已删除，绑定到已有手机号账号
                    log.warn("[weixinMiniLogin][绑定记录存在但用户已删除，绑定到已有手机号账号，socialUserId={}, mobile={}, partnerId={}]",
                            socialUser.getId(), mobile, existingPartnerByMobile.getId());
                    partner = existingPartnerByMobile;
                } else {
                    log.warn("[weixinMiniLogin][绑定记录存在但用户已删除，重新创建，socialUserId={}]", socialUser.getId());
                    partner = createWxMiniMember(socialUser, mobile);
                }
                bind.setUserId(partner.getId());
                socialUserBindMapper.updateById(bind);
            } else {
                log.info("[weixinMiniLogin][用户登录成功，userId={}]", partner.getId());
                // 只有前端传入了新昵称或头像才更新，避免用旧的社交信息覆盖用户已修改的资料
                if (StrUtil.isNotBlank(reqVO.getNickname()) || StrUtil.isNotBlank(reqVO.getAvatarUrl())) {
                    updatePartnerProfile(partner.getId(), nickname, avatarUrl, null);
                }
                partner = partnerService.getPartner(partner.getId());
            }
        }

        log.info("[weixinMiniLogin][微信小程序登录完成，userId={}]", partner.getId());
        return createTokenAfterLoginSuccess(partner, LoginLogTypeEnum.LOGIN_SOCIAL);
    }

    @Override
    public PartnerLoginRespVO refreshToken(String refreshToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken,
                OAUTH2_CLIENT_ID_MARRIAGE);
        return PartnerLoginRespVO.builder()
                .accessToken(accessTokenDO.getAccessToken())
                .refreshToken(accessTokenDO.getRefreshToken())
                .userId(accessTokenDO.getUserId())
                .build();
    }

    @Override
    public void sendSmsCode(PartnerSmsSendReqVO reqVO) {
        // 发送验证码
        SmsCodeSendReqDTO sendReqDTO = new SmsCodeSendReqDTO();
        sendReqDTO.setMobile(reqVO.getMobile());
        sendReqDTO.setScene(reqVO.getScene());
        sendReqDTO.setCreateIp(getClientIP());
        smsCodeApi.sendSmsCode(sendReqDTO);
    }

    // ==================== 私有方法 ====================

    /**
     * 手机 + 密码登录核心逻辑
     */
    private PartnerDO login0(String mobile, String password) {
        final LoginLogTypeEnum logTypeEnum = LoginLogTypeEnum.LOGIN_MOBILE;
        // 校验账号是否存在
        PartnerDO partner = partnerService.getPartnerByMobile(mobile);
        if (partner == null || !Boolean.TRUE.equals(partner.getIsMember())) {
            createLoginLog(null, mobile, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(PARTNER_MOBILE_PASSWORD_FAILED);
        }
        // 校验密码
        AdminUserDO user = adminUserService.getUser(partner.getId());
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            createLoginLog(partner.getId(), mobile, logTypeEnum, LoginResultEnum.BAD_CREDENTIALS);
            throw exception(PARTNER_MOBILE_PASSWORD_FAILED);
        }
        // 校验是否禁用
        if (CommonStatusEnum.isDisable(partner.getStatus())) {
            createLoginLog(partner.getId(), mobile, logTypeEnum, LoginResultEnum.USER_DISABLED);
            throw exception(AUTH_LOGIN_USER_DISABLED);
        }
        return partner;
    }

    /**
     * 登录成功后创建 Token 并记录日志
     */
    private PartnerLoginRespVO createTokenAfterLoginSuccess(PartnerDO partner, LoginLogTypeEnum logType) {
        // 插入登录日志
        createLoginLog(partner.getId(), partner.getMobile(), logType, LoginResultEnum.SUCCESS);
        // 更新最后登录时间
        adminUserService.updateUserLogin(partner.getId(), getClientIP());
        // 创建访问令牌
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(
                partner.getId(), UserTypeEnum.MEMBER.getValue(),
                OAUTH2_CLIENT_ID_MARRIAGE, null);
        // 构建返回结果
        return PartnerLoginRespVO.builder()
                .accessToken(accessTokenDO.getAccessToken())
                .refreshToken(accessTokenDO.getRefreshToken())
                .userId(partner.getId())
                .nickname(partner.getNickname())
                .avatar(partner.getAvatar())
                .build();
    }

    /**
     * 创建登录日志
     */
    private void createLoginLog(Long userId, String mobile, LoginLogTypeEnum logType, LoginResultEnum loginResult) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(logType.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        reqDTO.setUsername(mobile);
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(getClientIP());
        reqDTO.setResult(loginResult.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    /**
     * 创建登出日志
     */
    private void createLogoutLog(Long userId) {
        LoginLogCreateReqDTO reqDTO = new LoginLogCreateReqDTO();
        reqDTO.setLogType(LoginLogTypeEnum.LOGOUT_SELF.getType());
        reqDTO.setTraceId(TracerUtils.getTraceId());
        reqDTO.setUserId(userId);
        reqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        reqDTO.setUsername(getMobile(userId));
        reqDTO.setUserAgent(ServletUtils.getUserAgent());
        reqDTO.setUserIp(getClientIP());
        reqDTO.setResult(LoginResultEnum.SUCCESS.getResult());
        loginLogService.createLoginLog(reqDTO);
    }

    /**
     * 获取用户手机号
     */
    private String getMobile(Long userId) {
        if (userId == null) {
            return null;
        }
        PartnerDO partner = partnerService.getPartner(userId);
        return partner != null ? partner.getMobile() : null;
    }

    /**
     * 创建微信小程序会员
     */
    private PartnerDO createWxMiniMember(SocialUserDO socialUser, String mobile) {
        log.info("[createWxMiniMember][创建微信小程序会员，openid={}, mobile={}]", socialUser.getOpenid(), mobile);

        // 创建 Partner
        PartnerCreateReqVO partnerReqVO = new PartnerCreateReqVO();
        partnerReqVO.setNickname(socialUser.getNickname());
        partnerReqVO.setAvatar(socialUser.getAvatar());
        partnerReqVO.setMobile(mobile);
        partnerReqVO.setStatus((byte) CommonStatusEnum.ENABLE.getStatus().intValue());
        partnerReqVO.setIsMember(true);
        partnerReqVO.setIsCustomer(true);
        Long partnerId = partnerService.createPartner(partnerReqVO);
        log.info("[createWxMiniMember][创建Partner成功，partnerId={}]", partnerId);

        // 写死客户来源为"微信小程序注册"（字典 crm_customer_source, value=11）
        PartnerDO sourceUpdate = new PartnerDO();
        sourceUpdate.setId(partnerId);
        sourceUpdate.setSalesSource(11);
        partnerMapper.updateById(sourceUpdate);

        // 创建 AdminUser
        UserSaveReqVO userReqVO = new UserSaveReqVO();
        userReqVO.setPartnerId(partnerId);
        userReqVO.setUsername(buildWxUsername());
        userReqVO.setPassword(passwordEncoder.encode(RandomUtil.randomString(12)));
        adminUserService.createUser(userReqVO);

        // 初始化会员信息
        partnerMemberService.createMember(partnerId, getClientIP(), WebFrameworkUtils.getTerminal());

        return partnerService.getPartner(partnerId);
    }

    private void bindInviterIfPresent(Long partnerId, Long bindUserId) {
        if (bindUserId == null || bindUserId <= 0) {
            return;
        }
        if (!brokerageUserService.bindBrokerageUser(partnerId, bindUserId)) {
            throw exception(AUTH_WX_LOGIN_FAIL, "閭€璇峰叧绯荤粦瀹氬け璐ワ紝璇锋鏌ュ垎閿€閰嶇疆");
        }
    }

    /**
     * 更新 Partner 基本信息
     */
    private void updatePartnerProfile(Long partnerId, String nickname, String avatarUrl, String mobile) {
        PartnerDO updateObj = new PartnerDO();
        updateObj.setId(partnerId);
        updateObj.setIsMember(true);
        if (StrUtil.isNotBlank(nickname)) {
            updateObj.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(avatarUrl)) {
            updateObj.setAvatar(avatarUrl);
        }
        if (StrUtil.isNotBlank(mobile)) {
            // 检查手机号是否已被其他 partner 使用，冲突时跳过更新
            PartnerDO existingPartner = partnerMapper.selectByMobile(mobile);
            if (existingPartner == null || existingPartner.getId().equals(partnerId)) {
                updateObj.setMobile(mobile);
            } else {
                log.warn("[updatePartnerProfile][手机号已被其他用户使用，跳过更新，mobile={}, partnerId={}, existingId={}]",
                        mobile, partnerId, existingPartner.getId());
            }
        }
        partnerMapper.updateById(updateObj);
    }

    /**
     * 生成微信用户名
     */
    private String buildWxUsername() {
        return "wx" + System.currentTimeMillis() + RandomUtil.randomNumbers(4);
    }

    private record WxMiniRawUserInfo(String nickname, String avatarUrl) {
    }

}
