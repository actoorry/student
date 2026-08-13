package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBannerRespVO;
import vip.appap.suxin.module.sales.convert.SalesBannerConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBannerDO;
import vip.appap.suxin.module.sales.service.SalesBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/sales/promotion/banner")
@Tag(name = "用户 APP - 首页 Banner")
@Validated
public class AppSalesBannerController {

    @Resource
    private SalesBannerService bannerService;

    @GetMapping("/list")
    @Operation(summary = "获得 banner 列表")
    @Parameter(name = "position", description = "Banner position", example = "1")
    @PermitAll
    public CommonResult<List<AppSalesBannerRespVO>> getBannerList(@RequestParam("position") Integer position) {
        List<SalesBannerDO> bannerList = bannerService.getBannerListByPosition(position);
        return success(SalesBannerConvert.INSTANCE.convertList01(bannerList));
    }

    @PutMapping("/add-browse-count")
    @Operation(summary = "增加 Banner 点击量")
    @Parameter(name = "id", description = "Banner 编号", example = "1024")
    @PermitAll
    public CommonResult<Boolean> addBrowseCount(@RequestParam("id") Long id) {
        bannerService.addBannerBrowseCount(id);
        return success(true);
    }

}
