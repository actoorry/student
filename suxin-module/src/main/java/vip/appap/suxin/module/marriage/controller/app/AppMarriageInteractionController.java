package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageFollowCreateReqVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageFollowRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageInteractionStatisticsRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageRelationMemberRespVO;
import vip.appap.suxin.module.marriage.controller.app.vo.MarriageViewCreateReqVO;
import vip.appap.suxin.module.marriage.service.MarriageInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 婚恋互动")
@RestController
@RequestMapping("/marriage/interaction")
@Validated
public class AppMarriageInteractionController {

    @Resource
    private MarriageInteractionService marriageInteractionService;

    @PostMapping("/follow")
    @Operation(summary = "关注目标用户")
    public CommonResult<MarriageFollowRespVO> follow(@Valid @RequestBody MarriageFollowCreateReqVO createReqVO) {
        return success(marriageInteractionService.follow(getLoginUserId(), createReqVO.getRelPartnerId()));
    }

    @DeleteMapping("/unfollow")
    @Operation(summary = "取消关注目标用户")
    public CommonResult<Boolean> unfollow(@RequestParam("relPartnerId") Long relPartnerId) {
        marriageInteractionService.unfollow(getLoginUserId(), relPartnerId);
        return success(true);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取当前用户的互动统计")
    public CommonResult<MarriageInteractionStatisticsRespVO> getInteractionStatistics() {
        return success(marriageInteractionService.getInteractionStatistics(getLoginUserId()));
    }

    @GetMapping("/follow-me-page")
    @Operation(summary = "获取关注我的用户分页")
    public CommonResult<PageResult<MarriageRelationMemberRespVO>> getFollowMePage(@Valid PageParam pageParam) {
        return success(marriageInteractionService.getFollowMePage(getLoginUserId(), pageParam));
    }

    @GetMapping("/my-follow-page")
    @Operation(summary = "获取我关注的用户分页")
    public CommonResult<PageResult<MarriageRelationMemberRespVO>> getMyFollowPage(@Valid PageParam pageParam) {
        return success(marriageInteractionService.getMyFollowPage(getLoginUserId(), pageParam));
    }

    @PostMapping("/record-view")
    @Operation(summary = "记录浏览行为")
    public CommonResult<Boolean> recordView(@Valid @RequestBody MarriageViewCreateReqVO createReqVO) {
        marriageInteractionService.recordView(getLoginUserId(), createReqVO.getTargetPartnerId());
        return success(true);
    }

    @GetMapping("/view-me-page")
    @Operation(summary = "获取看过我的用户分页")
    public CommonResult<PageResult<MarriageRelationMemberRespVO>> getViewMePage(@Valid PageParam pageParam) {
        return success(marriageInteractionService.getViewMePage(getLoginUserId(), pageParam));
    }

    @GetMapping("/my-view-page")
    @Operation(summary = "获取我看过的用户分页")
    public CommonResult<PageResult<MarriageRelationMemberRespVO>> getMyViewPage(@Valid PageParam pageParam) {
        return success(marriageInteractionService.getMyViewPage(getLoginUserId(), pageParam));
    }

}
