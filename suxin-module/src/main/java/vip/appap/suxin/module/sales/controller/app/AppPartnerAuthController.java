package vip.appap.suxin.module.sales.controller.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.partner.controller.app.vo.*;
import vip.appap.suxin.module.sales.service.PartnerAuthService;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "App - 合作伙伴认证")
@RestController
@RequestMapping("/partner/auth")
@Validated
public class AppPartnerAuthController {

    @Resource
    private PartnerAuthService partnerAuthService;

    @PermitAll
    @PostMapping("/login")
    @Operation(summary = "手机号 + 密码登录")
    public CommonResult<PartnerLoginRespVO> login(@Valid @RequestBody PartnerLoginReqVO reqVO) {
        return success(partnerAuthService.login(reqVO));
    }

    @PermitAll
    @PostMapping("/register")
    @Operation(summary = "手机号注册")
    public CommonResult<Long> register(@Valid @RequestBody PartnerRegisterReqVO reqVO) {
        return success(partnerAuthService.register(reqVO));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    public CommonResult<Boolean> logout(@RequestParam("accessToken") String accessToken,
                                        @RequestParam("refreshToken") String refreshToken) {
        partnerAuthService.logout(accessToken);
        return success(true);
    }

    @PermitAll
    @PostMapping("/sms-login")
    @Operation(summary = "手机 + 验证码登录")
    public CommonResult<PartnerLoginRespVO> smsLogin(@Valid @RequestBody PartnerSmsLoginReqVO reqVO) {
        return success(partnerAuthService.smsLogin(reqVO));
    }

    @PermitAll
    @PostMapping("/social-login")
    @Operation(summary = "社交登录，使用 code 授权码")
    public CommonResult<PartnerLoginRespVO> socialLogin(@Valid @RequestBody PartnerSocialLoginReqVO reqVO) {
        return success(partnerAuthService.socialLogin(reqVO));
    }

    @PermitAll
    @PostMapping("/weixin-mini-login")
    @Operation(summary = "微信小程序一键登录")
    public CommonResult<PartnerLoginRespVO> weixinMiniLogin(@Valid @RequestBody PartnerWxMiniLoginReqVO reqVO) {
        return success(partnerAuthService.weixinMiniLogin(reqVO));
    }

    @PermitAll
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌")
    public CommonResult<PartnerLoginRespVO> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return success(partnerAuthService.refreshToken(refreshToken));
    }

    @PermitAll
    @PostMapping("/send-sms-code")
    @Operation(summary = "发送手机验证码")
    public CommonResult<Boolean> sendSmsCode(@Valid @RequestBody PartnerSmsSendReqVO reqVO) {
        partnerAuthService.sendSmsCode(reqVO);
        return success(true);
    }

}
