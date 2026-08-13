package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesDiscountActivityUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesDiscountActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiscountProductDO;
import vip.appap.suxin.module.sales.service.SalesDiscountActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 限时折扣活动")
@RestController
@RequestMapping("/sales/promotion/discount-activity")
@Validated
public class SalesDiscountActivityController {

    @Resource
    private SalesDiscountActivityService discountActivityService;

    @PostMapping("/create")
    @Operation(summary = "创建限时折扣活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_discount_activity:create')")
    public CommonResult<Long> createDiscountActivity(@Valid @RequestBody SalesDiscountActivityCreateReqVO createReqVO) {
        return success(discountActivityService.createDiscountActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新限时折扣活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_discount_activity:update')")
    public CommonResult<Boolean> updateDiscountActivity(@Valid @RequestBody SalesDiscountActivityUpdateReqVO updateReqVO) {
        discountActivityService.updateDiscountActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭限时折扣活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_discount_activity:close')")
    public CommonResult<Boolean> closeRewardActivity(@RequestParam("id") Long id) {
        discountActivityService.closeDiscountActivity(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除限时折扣活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_discount_activity:delete')")
    public CommonResult<Boolean> deleteDiscountActivity(@RequestParam("id") Long id) {
        discountActivityService.deleteDiscountActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得限时折扣活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_discount_activity:query')")
    public CommonResult<SalesDiscountActivityRespVO> getDiscountActivity(@RequestParam("id") Long id) {
        SalesDiscountActivityDO discountActivity = discountActivityService.getDiscountActivity(id);
        if (discountActivity == null) {
            return success(null);
        }
        // 拼接结果
        List<SalesDiscountProductDO> discountProducts = discountActivityService.getDiscountProductsByActivityId(id);
        return success(SalesDiscountActivityConvert.INSTANCE.convert(discountActivity, discountProducts));
    }

    @GetMapping("/page")
    @Operation(summary = "获得限时折扣活动分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_discount_activity:query')")
    public CommonResult<PageResult<SalesDiscountActivityRespVO>> getDiscountActivityPage(@Valid SalesDiscountActivityPageReqVO pageVO) {
        PageResult<SalesDiscountActivityDO> pageResult = discountActivityService.getDiscountActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<SalesDiscountProductDO> products = discountActivityService.getDiscountProductsByActivityId(
                convertSet(pageResult.getList(), SalesDiscountActivityDO::getId));
        return success(SalesDiscountActivityConvert.INSTANCE.convertPage(pageResult, products));
    }

}
