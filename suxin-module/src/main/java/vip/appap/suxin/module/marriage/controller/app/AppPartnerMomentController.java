package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCommentCreateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCommentPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCommentRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentCreateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentLikeReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentLikeRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentPageReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.AppPartnerMomentRespVO;
import vip.appap.suxin.module.marriage.service.PartnerMomentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 动态")
@RestController
@RequestMapping("/marriage/moment")
@Validated
public class AppPartnerMomentController {

    @Resource
    private PartnerMomentService partnerMomentService;

    @GetMapping("/page")
    @Operation(summary = "获取动态分页")
    @PermitAll
    public CommonResult<PageResult<AppPartnerMomentRespVO>> getPartnerMomentPage(
            @Valid AppPartnerMomentPageReqVO pageReqVO) {
        return success(partnerMomentService.getPartnerMomentPage(pageReqVO, getLoginUserId()));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获取我的动态分页")
    public CommonResult<PageResult<AppPartnerMomentRespVO>> getMyPartnerMomentPage(
            @Valid AppPartnerMomentPageReqVO pageReqVO) {
        return success(partnerMomentService.getMyPartnerMomentPage(pageReqVO, getLoginUserId()));
    }

    @PostMapping("/create")
    @Operation(summary = "发布动态")
    public CommonResult<Long> createPartnerMoment(@Valid @RequestBody AppPartnerMomentCreateReqVO createReqVO) {
        return success(partnerMomentService.createPartnerMoment(createReqVO, getLoginUserId()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除自己的动态")
    public CommonResult<Boolean> deletePartnerMoment(@RequestParam("id") Long id) {
        partnerMomentService.deletePartnerMoment(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/like")
    @Operation(summary = "点赞或取消点赞动态")
    public CommonResult<AppPartnerMomentLikeRespVO> togglePartnerMomentLike(
            @Valid @RequestBody AppPartnerMomentLikeReqVO reqVO) {
        return success(partnerMomentService.togglePartnerMomentLike(reqVO, getLoginUserId()));
    }

    @GetMapping("/comment/page")
    @Operation(summary = "获取动态评论分页")
    @PermitAll
    public CommonResult<PageResult<AppPartnerMomentCommentRespVO>> getPartnerMomentCommentPage(
            @Valid AppPartnerMomentCommentPageReqVO pageReqVO) {
        return success(partnerMomentService.getPartnerMomentCommentPage(pageReqVO));
    }

    @PostMapping("/comment/create")
    @Operation(summary = "创建动态评论")
    public CommonResult<AppPartnerMomentCommentRespVO> createPartnerMomentComment(
            @Valid @RequestBody AppPartnerMomentCommentCreateReqVO createReqVO) {
        return success(partnerMomentService.createPartnerMomentComment(createReqVO, getLoginUserId()));
    }

}
