package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesRewardActivityUpdateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesRewardActivityDO;
import vip.appap.suxin.module.sales.service.SalesRewardActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 满减送活动")
@RestController
@RequestMapping("/sales/promotion/reward-activity")
@Validated
public class SalesRewardActivityController {

    @Resource
    private SalesRewardActivityService rewardActivityService;

    @PostMapping("/create")
    @Operation(summary = "创建满减送活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_reward_activity:create')")
    public CommonResult<Long> createRewardActivity(@Valid @RequestBody SalesRewardActivityCreateReqVO createReqVO) {
        return success(rewardActivityService.createRewardActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新满减送活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_reward_activity:update')")
    public CommonResult<Boolean> updateRewardActivity(@Valid @RequestBody SalesRewardActivityUpdateReqVO updateReqVO) {
        rewardActivityService.updateRewardActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭满减送活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_reward_activity:close')")
    public CommonResult<Boolean> closeRewardActivity(@RequestParam("id") Long id) {
        rewardActivityService.closeRewardActivity(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除满减送活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_reward_activity:delete')")
    public CommonResult<Boolean> deleteRewardActivity(@RequestParam("id") Long id) {
        rewardActivityService.deleteRewardActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得满减送活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_reward_activity:query')")
    public CommonResult<SalesRewardActivityRespVO> getRewardActivity(@RequestParam("id") Long id) {
        SalesRewardActivityDO rewardActivity = rewardActivityService.getRewardActivity(id);
        return success(BeanUtils.toBean(rewardActivity, SalesRewardActivityRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得满减送活动分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_reward_activity:query')")
    public CommonResult<PageResult<SalesRewardActivityRespVO>> getRewardActivityPage(@Valid SalesRewardActivityPageReqVO pageVO) {
        PageResult<SalesRewardActivityDO> pageResult = rewardActivityService.getRewardActivityPage(pageVO);
        return success(BeanUtils.toBean(pageResult, SalesRewardActivityRespVO.class));
    }

}
