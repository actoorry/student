package vip.appap.suxin.module.product.controller.admin;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.controller.admin.vo.*;
import vip.appap.suxin.module.product.convert.ProductUnitConvert;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.service.ProductUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 产品单位")
@RestController
@RequestMapping("/product/unit")
@Validated
public class ProductUnitController {

    @Resource
    private ProductUnitService unitService;

    @PostMapping("/create")
    @Operation(summary = "创建单位")
    @PreAuthorize("@ss.hasPermission('product:unit:create')")
    public CommonResult<Long> createUnit(@Valid @RequestBody ProductUnitCreateReqVO createReqVO) {
        return success(unitService.createUnit(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新单位")
    @PreAuthorize("@ss.hasPermission('product:unit:update')")
    public CommonResult<Boolean> updateUnit(@Valid @RequestBody ProductUnitUpdateReqVO updateReqVO) {
        unitService.updateUnit(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除单位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:unit:delete')")
    public CommonResult<Boolean> deleteUnit(@RequestParam("id") Long id) {
        unitService.deleteUnit(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得单位")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:unit:query')")
    public CommonResult<ProductUnitRespVO> getUnit(@RequestParam("id") Long id) {
        ProductUnitDO unit = unitService.getUnit(id);
        return success(ProductUnitConvert.INSTANCE.convert(unit));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取单位精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<ProductUnitSimpleRespVO>> getSimpleUnitList() {
        // 获取单位列表，只要开启状态的
        List<ProductUnitDO> list = unitService.getUnitListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(ProductUnitConvert.INSTANCE.convertList1(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得单位分页")
    @PreAuthorize("@ss.hasPermission('product:unit:query')")
    public CommonResult<PageResult<ProductUnitRespVO>> getUnitPage(@Valid ProductUnitPageReqVO pageVO) {
        PageResult<ProductUnitDO> pageResult = unitService.getUnitPage(pageVO);
        return success(ProductUnitConvert.INSTANCE.convertPage(pageResult));
    }

}
