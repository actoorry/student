package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBannerUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesBannerConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBannerDO;
import vip.appap.suxin.module.sales.service.SalesBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - Banner 管理")
@RestController
@RequestMapping("/sales/promotion/banner")
@Validated
public class SalesBannerController {

    @Resource
    private SalesBannerService bannerService;

    @PostMapping("/create")
    @Operation(summary = "创建 Banner")
    @PreAuthorize("@ss.hasPermission('sales:sales_banner:create')")
    public CommonResult<Long> createBanner(@Valid @RequestBody SalesBannerCreateReqVO createReqVO) {
        return success(bannerService.createBanner(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 Banner")
    @PreAuthorize("@ss.hasPermission('sales:sales_banner:update')")
    public CommonResult<Boolean> updateBanner(@Valid @RequestBody SalesBannerUpdateReqVO updateReqVO) {
        bannerService.updateBanner(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 Banner")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_banner:delete')")
    public CommonResult<Boolean> deleteBanner(@RequestParam("id") Long id) {
        bannerService.deleteBanner(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 Banner")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_banner:query')")
    public CommonResult<SalesBannerRespVO> getBanner(@RequestParam("id") Long id) {
        SalesBannerDO banner = bannerService.getBanner(id);
        return success(SalesBannerConvert.INSTANCE.convert(banner));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 Banner 分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_banner:query')")
    public CommonResult<PageResult<SalesBannerRespVO>> getBannerPage(@Valid SalesBannerPageReqVO pageVO) {
        PageResult<SalesBannerDO> pageResult = bannerService.getBannerPage(pageVO);
        return success(SalesBannerConvert.INSTANCE.convertPage(pageResult));
    }

}
