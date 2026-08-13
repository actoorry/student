package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.controller.app.vo.*;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import jakarta.annotation.security.PermitAll;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.appap.suxin.module.partner.service.PartnerService;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 用户个人中心")
@RestController
@RequestMapping("/partner/user")
@Validated
@Slf4j
public class AppPartnerUserController {

    @Resource
    private PartnerService partnerService;

    @GetMapping("/get")
    @Operation(summary = "获得基本信息")
    public CommonResult<AppPartnerUserInfoRespVO> getUserInfo() {
        PartnerDO partner = partnerService.getPartner(getLoginUserId());
        return success(BeanUtils.toBean(partner, AppPartnerUserInfoRespVO.class));
    }

    @PutMapping("/update")
    @Operation(summary = "修改基本信息")
    public CommonResult<Boolean> updateUser(@RequestBody @Valid AppPartnerUserUpdateReqVO reqVO) {
        partnerService.updatePartnerProfile(getLoginUserId(), reqVO.getNickname(), reqVO.getAvatar());
        return success(true);
    }

    @PutMapping("/update-mobile")
    @Operation(summary = "修改用户手机")
    public CommonResult<Boolean> updateUserMobile(@RequestBody @Valid AppPartnerUserUpdateMobileReqVO reqVO) {
        partnerService.updatePartnerMobile(getLoginUserId(), reqVO.getMobile(), reqVO.getCode());
        return success(true);
    }

    @PutMapping("/update-mobile-by-weixin")
    @Operation(summary = "基于微信小程序授权码修改手机号")
    public CommonResult<Boolean> updateUserMobileByWeixin(@RequestBody @Valid AppPartnerUserUpdateMobileByWeixinReqVO reqVO) {
        partnerService.updatePartnerMobileByWeixin(getLoginUserId(), reqVO.getCode());
        return success(true);
    }

    @PutMapping("/update-password")
    @Operation(summary = "修改用户密码")
    public CommonResult<Boolean> updateUserPassword(@RequestBody @Valid AppPartnerUserUpdatePasswordReqVO reqVO) {
        partnerService.updatePartnerPassword(getLoginUserId(), reqVO.getCode(), reqVO.getPassword());
        return success(true);
    }

    @PutMapping("/reset-password")
    @Operation(summary = "重置密码")
    @PermitAll
    public CommonResult<Boolean> resetUserPassword(@RequestBody @Valid AppPartnerUserResetPasswordReqVO reqVO) {
        partnerService.resetPartnerPassword(reqVO.getMobile(), reqVO.getCode(), reqVO.getPassword());
        return success(true);
    }

}
