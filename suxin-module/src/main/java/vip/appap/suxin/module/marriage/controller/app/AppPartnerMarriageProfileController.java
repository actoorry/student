package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfilePageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMarriageProfileRespVO;
import vip.appap.suxin.module.marriage.service.PartnerMarriageProfileService;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import vip.appap.suxin.module.partner.dal.mysql.PartnerMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 婚恋首页推荐")
@RestController
@RequestMapping("/marriage/recommend-member")
@Validated
public class AppPartnerMarriageProfileController {

    @Resource
    private PartnerMarriageProfileService partnerMarriageProfileService;
    @Resource
    private PartnerMapper partnerMapper;

    @GetMapping("/page")
    @Operation(summary = "获取首页推荐会员分页")
    @PermitAll
    public CommonResult<PageResult<AppPartnerMarriageProfileRespVO>> getPartnerMarriageProfilePage(
            @Valid AppPartnerMarriageProfilePageReqVO pageReqVO) {
        // 获取当前登录用户
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Integer loginUserSex = null;
        if (loginUserId != null) {
            // 查询当前用户的性别
            PartnerDO loginPartner = partnerMapper.selectById(loginUserId);
            if (loginPartner != null) {
                loginUserSex = loginPartner.getSex();
            }
        }
        return success(partnerMarriageProfileService.getPartnerMarriageProfilePage(pageReqVO, loginUserId, loginUserSex));
    }

    @GetMapping("/get")
    @Operation(summary = "获取会员个人页面")
    @PermitAll
    public CommonResult<AppPartnerMarriageProfileRespVO> getPartnerMarriageProfile(@RequestParam("id") Long id) {
        return success(partnerMarriageProfileService.getPartnerMarriageProfile(id));
    }

}
