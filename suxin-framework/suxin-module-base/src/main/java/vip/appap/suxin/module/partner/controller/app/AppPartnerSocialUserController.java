package vip.appap.suxin.module.partner.controller.app;

import cn.hutool.core.codec.Base64;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.system.api.SocialClientApi;
import vip.appap.suxin.module.system.api.SocialUserApi;
import vip.appap.suxin.module.system.api.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 社交用户")
@RestController
@RequestMapping("/partner/social-user")
@Validated
public class AppPartnerSocialUserController {

    @Resource
    private SocialUserApi socialUserApi;
    @Resource
    private SocialClientApi socialClientApi;

    @PostMapping("/bind")
    @Operation(summary = "社交绑定，使用 code 授权码")
    @PermitAll
    public CommonResult<String> socialBind(@RequestBody @Valid SocialUserBindReqDTO reqDTO) {
        reqDTO.setUserId(getLoginUserId());
        reqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        String openid = socialUserApi.bindSocialUser(reqDTO);
        return success(openid);
    }

    @DeleteMapping("/unbind")
    @Operation(summary = "取消社交绑定")
    public CommonResult<Boolean> socialUnbind(@RequestBody @Valid SocialUserUnbindReqDTO reqDTO) {
        reqDTO.setUserId(getLoginUserId());
        reqDTO.setUserType(UserTypeEnum.MEMBER.getValue());
        socialUserApi.unbindSocialUser(reqDTO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得社交用户")
    @Parameter(name = "type", description = "社交平台的类型", required = true, example = "10")
    public CommonResult<SocialUserRespDTO> getSocialUser(@RequestParam("type") Integer type) {
        SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(UserTypeEnum.MEMBER.getValue(), getLoginUserId(), type);
        return success(socialUser);
    }

    @PostMapping("/wxa-qrcode")
    @Operation(summary = "获得微信小程序码(base64 image)")
    @PermitAll
    public CommonResult<String> getWxaQrcode(@RequestBody @Valid SocialWxQrcodeReqDTO reqDTO) {
        byte[] wxQrcode = socialClientApi.getWxaQrcode(reqDTO);
        return success(Base64.encode(wxQrcode));
    }

    @GetMapping("/get-subscribe-template-list")
    @Operation(summary = "获得微信小程序订阅模板列表")
    @PermitAll
    public CommonResult<List<SocialWxaSubscribeTemplateRespDTO>> getSubscribeTemplateList() {
        List<SocialWxaSubscribeTemplateRespDTO> templates = socialClientApi.getWxaSubscribeTemplateList(UserTypeEnum.MEMBER.getValue());
        return success(templates);
    }

}
