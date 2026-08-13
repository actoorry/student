package vip.appap.suxin.module.wms.controller.admin.product;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.*;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.wms.service.product.WmsProductService;
import vip.appap.suxin.module.wms.service.product.WmsProductSearchService;

@Tag(name = "管理后台 - WMS 产品查询（跨模块调用 ProductApi）")
@RestController
@RequestMapping("/wms/product")
@Validated
public class WmsProductController {

    @Resource
    private WmsProductService wmsProductService;

    @Resource
    private WmsProductSearchService wmsProductSearchService;

    @GetMapping("/search-sku")
    @Operation(summary = "模糊搜索 SKU（用于下拉远程搜索，支持商品名称、条码、SKU ID）")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<List<Map<String, Object>>> searchSku(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        List<Map<String, Object>> list = wmsProductSearchService.searchSku(keyword, limit);
        if (list.isEmpty()) {
            // 空结果时返回空数组
            return success(Collections.emptyList());
        }
        return success(list);
    }

    @GetMapping("/get-sku")
    @Operation(summary = "查询 SKU 信息")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<Map<String, Object>> getSku(@RequestParam("id") Long id) {
        List<Map<String, Object>> list = wmsProductSearchService.getSkuListByIds(Collections.singletonList(id));
        if (list.isEmpty()) return success(Collections.emptyMap());
        return success(list.get(0));
    }

    @GetMapping("/list-sku")
    @Operation(summary = "批量查询 SKU 列表")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<List<Map<String, Object>>> getSkuList(@RequestParam("ids") String ids) {
        List<Long> idList = new ArrayList<>();
        for (String s : ids.split(",")) {
            try { idList.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) { }
        }
        return success(wmsProductSearchService.getSkuListByIds(idList));
    }

    @GetMapping("/sku-name")
    @Operation(summary = "获取 SKU 显示名称")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<String> getSkuName(@RequestParam("id") Long id) {
        return success(wmsProductService.getSkuDisplayName(id));
    }

}