package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesCouponTemplateConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponTemplateDO;
import vip.appap.suxin.module.sales.service.SalesCouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 优惠劵模板")
@RestController
@RequestMapping("/sales/promotion/coupon-template")
@Validated
public class SalesCouponTemplateController {

    @Resource
    private SalesCouponTemplateService couponTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建优惠劵模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:create')")
    public CommonResult<Long> createCouponTemplate(@Valid @RequestBody SalesCouponTemplateCreateReqVO createReqVO) {
        return success(couponTemplateService.createCouponTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新优惠劵模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:update')")
    public CommonResult<Boolean> updateCouponTemplate(@Valid @RequestBody SalesCouponTemplateUpdateReqVO updateReqVO) {
        couponTemplateService.updateCouponTemplate(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新优惠劵模板状态")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:update')")
    public CommonResult<Boolean> updateCouponTemplateStatus(@Valid @RequestBody SalesCouponTemplateUpdateStatusReqVO reqVO) {
        couponTemplateService.updateCouponTemplateStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除优惠劵模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:delete')")
    public CommonResult<Boolean> deleteCouponTemplate(@RequestParam("id") Long id) {
        couponTemplateService.deleteCouponTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得优惠劵模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:query')")
    public CommonResult<SalesCouponTemplateRespVO> getCouponTemplate(@RequestParam("id") Long id) {
        SalesCouponTemplateDO couponTemplate = couponTemplateService.getCouponTemplate(id);
        return success(SalesCouponTemplateConvert.INSTANCE.convert(couponTemplate));
    }

    @GetMapping("/page")
    @Operation(summary = "获得优惠劵模板分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:query')")
    public CommonResult<PageResult<SalesCouponTemplateRespVO>> getCouponTemplatePage(@Valid SalesCouponTemplatePageReqVO pageVO) {
        PageResult<SalesCouponTemplateDO> pageResult = couponTemplateService.getCouponTemplatePage(pageVO);
        return success(SalesCouponTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得优惠劵模板列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('sales:sales_coupon_template:query')")
    public CommonResult<List<SalesCouponTemplateRespVO>> getCouponTemplateList(@RequestParam("ids") Collection<Long> ids) {
        List<SalesCouponTemplateDO> list = couponTemplateService.getCouponTemplateList(ids);
        return success(SalesCouponTemplateConvert.INSTANCE.convertList(list));
    }

}
