package vip.appap.suxin.module.system.controller.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.datapermission.core.annotation.DataPermission;
import vip.appap.suxin.framework.security.config.SecurityProperties;
import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.module.partner.service.PartnerService;
import vip.appap.suxin.module.system.controller.admin.vo.*;
import vip.appap.suxin.module.system.convert.AuthConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.system.dal.dataobject.MenuDO;
import vip.appap.suxin.module.system.dal.dataobject.RoleDO;
import vip.appap.suxin.module.system.dal.dataobject.AdminUserDO;
import vip.appap.suxin.module.system.enums.LoginLogTypeEnum;
import vip.appap.suxin.module.system.service.AdminAuthService;
import vip.appap.suxin.module.system.service.MenuService;
import vip.appap.suxin.module.system.service.PermissionService;
import vip.appap.suxin.module.system.service.RoleService;
import vip.appap.suxin.module.system.service.SocialClientService;
import vip.appap.suxin.module.system.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.HashSet;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 认证")
@RestController
@RequestMapping("/system/auth")
@Validated
@Slf4j
public class AuthController {

    @Resource
    private AdminAuthService authService;
    @Resource
    private AdminUserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private PartnerService partnerService;
    @Resource
    private SocialClientService socialClientService;

    @Resource
    private SecurityProperties securityProperties;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用账号密码登录")
    public CommonResult<AuthLoginRespVO> login(@RequestBody @Valid AuthLoginReqVO reqVO) {
        return success(authService.login(reqVO));
    }

    @PostMapping("/logout")
    @PermitAll
    @Operation(summary = "登出系统")
    public CommonResult<Boolean> logout(HttpServletRequest request) {
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (StrUtil.isNotBlank(token)) {
            authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        }
        return success(true);
    }

    @PostMapping("/refresh-token")
    @PermitAll
    @Operation(summary = "刷新令牌")
    @Parameter(name = "refreshToken", description = "刷新令牌", required = true)
    public CommonResult<AuthLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(authService.refreshToken(refreshToken));
    }

    @GetMapping("/get-permission-info")
    @Operation(summary = "获取登录用户的权限信息")
    @DataPermission(enable = false) // 忽略数据权限，避免因为过滤，导致无法查询用户。类似：https://t.zsxq.com/LHnrp
    public CommonResult<AuthPermissionInfoRespVO> getPermissionInfo() {
        // 1.1 获得用户信息
        AdminUserDO user = userService.getUser(getLoginUserId());
        if (user == null) {
            return success(null);
        }

        // 1.2 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(getLoginUserId());
        AuthPermissionInfoRespVO result;
        if (CollUtil.isEmpty(roleIds)) {
            result = AuthConvert.INSTANCE.convert(user, Collections.emptyList(), Collections.emptyList());
        } else {
            List<RoleDO> roles = roleService.getRoleList(roleIds);
            roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())); // 移除禁用的角色

            // 1.3 获得菜单列表
            Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(convertSet(roles, RoleDO::getId));
            List<MenuDO> menuList = menuService.getMenuList(menuIds);
            menuList = menuService.filterDisableMenus(menuList);

            // 2. 拼接结果返回
            result = AuthConvert.INSTANCE.convert(user, roles, menuList);
            // 2.1 超管赋予所有权限
            if (roleService.hasAnySuperAdmin(convertSet(roles, RoleDO::getId))) {
                result.setPermissions(new HashSet<>(Arrays.asList("*:*:*")));
            }
        }
        // 2.2 从 partner 补充昵称、头像等（system_users.id = partner.id）
        enrichUserFromPartner(result);
        return success(result);
    }

    /**
     * 从 partner 补充用户展示信息
     */
    private void enrichUserFromPartner(AuthPermissionInfoRespVO result) {
        if (result == null || result.getUser() == null) {
            return;
        }
        PartnerDO partner = partnerService.getPartner(result.getUser().getId());
        if (partner == null) {
            return;
        }
        AuthPermissionInfoRespVO.UserVO userVO = result.getUser();
        userVO.setNickname(partner.getNickname());
        userVO.setAvatar(partner.getAvatar());
        userVO.setEmail(partner.getEmail());
    }

    @PostMapping("/register")
    @PermitAll
    @Operation(summary = "注册用户")
    public CommonResult<AuthLoginRespVO> register(@RequestBody @Valid AuthRegisterReqVO registerReqVO) {
        return success(authService.register(registerReqVO));
    }

    // ========== 短信登录相关 ==========

    @PostMapping("/sms-login")
    @PermitAll
    @Operation(summary = "使用短信验证码登录")
    // 可按需开启限流：https://github.com/YunaiV/ruoyi-vue-pro/issues/851
    // @RateLimiter(time = 60, count = 6, keyResolver = ExpressionRateLimiterKeyResolver.class, keyArg = "#reqVO.mobile")
    public CommonResult<AuthLoginRespVO> smsLogin(@RequestBody @Valid AuthSmsLoginReqVO reqVO) {
        return success(authService.smsLogin(reqVO));
    }

    @PostMapping("/send-sms-code")
    @PermitAll
    @Operation(summary = "发送手机验证码")
    public CommonResult<Boolean> sendLoginSmsCode(@RequestBody @Valid AuthSmsSendReqVO reqVO) {
        authService.sendSmsCode(reqVO);
        return success(true);
    }

    @PostMapping("/reset-password")
    @PermitAll
    @Operation(summary = "重置密码")
    public CommonResult<Boolean> resetPassword(@RequestBody @Valid AuthResetPasswordReqVO reqVO) {
        authService.resetPassword(reqVO);
        return success(true);
    }

    // ========== 社交登录相关 ==========

    @GetMapping("/social-auth-redirect")
    @PermitAll
    @Operation(summary = "社交授权的跳转")
    @Parameters({
            @Parameter(name = "type", description = "社交类型", required = true),
            @Parameter(name = "redirectUri", description = "回调路径")
    })
    public CommonResult<String> socialLogin(@RequestParam("type") Integer type,
                                            @RequestParam("redirectUri") String redirectUri) {
        return success(socialClientService.getAuthorizeUrl(
                type, UserTypeEnum.ADMIN.getValue(), redirectUri));
    }

    @PostMapping("/social-login")
    @PermitAll
    @Operation(summary = "社交快捷登录，使用 code 授权码", description = "适合未登录的用户，但是社交账号已绑定用户")
    public CommonResult<AuthLoginRespVO> socialQuickLogin(@RequestBody @Valid AuthSocialLoginReqVO reqVO) {
        return success(authService.socialLogin(reqVO));
    }

}
