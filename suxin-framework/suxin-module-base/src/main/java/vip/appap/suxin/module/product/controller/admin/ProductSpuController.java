package vip.appap.suxin.module.product.controller.admin;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.product.controller.admin.vo.*;
import vip.appap.suxin.module.product.convert.ProductSpuConvert;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.product.service.ProductSkuService;
import vip.appap.suxin.module.product.service.ProductSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.pojo.PageParam.PAGE_SIZE_NONE;

@Tag(name = "管理后台 - 商品 SPU")
@RestController
@RequestMapping("/product/spu")
@Validated
public class ProductSpuController {

    @Resource
    private ProductSpuService productSpuService;
    @Resource
    private ProductSkuService productSkuService;

    @PostMapping("/create")
    @Operation(summary = "创建商品 SPU")
    @PreAuthorize("@ss.hasPermission('product:spu:create')")
    public CommonResult<Long> createProductSpu(@Valid @RequestBody ProductSpuSaveReqVO createReqVO) {
        return success(productSpuService.createSpu(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新商品 SPU")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> updateSpu(@Valid @RequestBody ProductSpuSaveReqVO updateReqVO) {
        productSpuService.updateSpu(updateReqVO);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "更新商品 SPU Status")
    @PreAuthorize("@ss.hasPermission('product:spu:update')")
    public CommonResult<Boolean> updateStatus(@Valid @RequestBody ProductSpuUpdateStatusReqVO updateReqVO) {
        productSpuService.updateSpuStatus(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除商品 SPU")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:spu:delete')")
    public CommonResult<Boolean> deleteSpu(@RequestParam("id") Long id) {
        productSpuService.deleteSpu(id);
        return success(true);
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得商品 SPU 明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<ProductSpuRespVO> getSpuDetail(@RequestParam("id") Long id) {
        // 获得商品 SPU
        ProductSpuDO spu = productSpuService.getSpu(id);
        if (spu == null) {
            return success(null);
        }
        // 查询商品 SKU
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(spu.getId());
        return success(ProductSpuConvert.INSTANCE.convert(spu, skus));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得商品 SPU 精简列表")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<ProductSpuSimpleRespVO>> getSpuSimpleList() {
        List<ProductSpuDO> list = productSpuService.getSpuListByStatus(ProductSpuStatusEnum.ENABLE.getStatus());
        // 降序排序后，返回给前端
        list.sort(Comparator.comparing(ProductSpuDO::getSort).reversed());
        return success(BeanUtils.toBean(list, ProductSpuSimpleRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得商品 SPU 详情列表")
    @Parameter(name = "spuIds", description = "spu 编号列表", required = true, example = "[1,2,3]")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<List<ProductSpuRespVO>> getSpuList(@RequestParam("spuIds") Collection<Long> spuIds) {
        return success(ProductSpuConvert.INSTANCE.convertForSpuDetailRespListVO(
                productSpuService.getSpuList(spuIds), productSkuService.getSkuListBySpuId(spuIds)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得商品 SPU 分页")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<PageResult<ProductSpuRespVO>> getSpuPage(@Valid ProductSpuPageReqVO pageVO) {
        PageResult<ProductSpuDO> pageResult = productSpuService.getSpuPage(pageVO);
        // 查询 SKU 并填充微信道具等字段，确保列表页状态展示准确
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(
                convertSet(pageResult.getList(), ProductSpuDO::getId));
        PageResult<ProductSpuRespVO> voPageResult = new PageResult<>();
        voPageResult.setTotal(pageResult.getTotal());
        voPageResult.setList(ProductSpuConvert.INSTANCE.convertForSpuDetailRespListVO(pageResult.getList(), skus));
        return success(voPageResult);
    }

    @GetMapping("/get-count")
    @Operation(summary = "获得商品 SPU 分页 tab count")
    @PreAuthorize("@ss.hasPermission('product:spu:query')")
    public CommonResult<Map<Integer, Long>> getSpuCount() {
        return success(productSpuService.getTabsCount());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出商品")
    @PreAuthorize("@ss.hasPermission('product:spu:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSpuList(@Validated ProductSpuPageReqVO reqVO,
                               HttpServletResponse response) throws IOException {
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<ProductSpuDO> list = productSpuService.getSpuPage(reqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "商品列表.xls", "数据", ProductSpuRespVO.class,
                BeanUtils.toBean(list, ProductSpuRespVO.class));
    }

}
