package vip.appap.suxin.module.partner.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberConfigRespVO;
import vip.appap.suxin.module.partner.controller.app.vo.AppPartnerMemberRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.appap.suxin.module.partner.service.PartnerMemberService;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 会员")
@RestController
@RequestMapping("/partner/member")
@Validated
public class AppPartnerMemberController {

    @Resource
    private PartnerMemberService partnerMemberService;

    @GetMapping("/get-my-membership")
    @Operation(summary = "获得我的会员")
    public CommonResult<AppPartnerMemberRespVO> getMyMembership() {
        return success(partnerMemberService.getMyMembership(getLoginUserId()));
    }

    @GetMapping("/get-config")
    @Operation(summary = "获得会员配置")
    @PermitAll
    public CommonResult<AppPartnerMemberConfigRespVO> getMemberConfig() {
        return success(partnerMemberService.getMemberConfig());
    }

}
