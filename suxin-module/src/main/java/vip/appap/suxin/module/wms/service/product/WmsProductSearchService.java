package vip.appap.suxin.module.wms.service.product;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WMS 产品搜索服务（通过 ProductApi 跨模块调用）
 *
 * 规范：
 * 1. 禁止直接引入 product 模块的 ServiceImpl、DO 实体
 * 2. 只能使用 ProductSkuApi / ProductSpuApi 对外接口
 * 3. 只读不写：仅调用查询接口
 * 4. sku_id 为 FK → product_sku.id，不可修改 product 侧数据
 */
@Service
@Validated
public class WmsProductSearchService {

    @Resource
    private ProductSkuApi productSkuApi;

    @Resource
    private ProductSpuApi productSpuApi;

    /**
     * 模糊搜索 SKU 列表（支持按商品名称、SKU ID 搜索）
     * 通过 ProductApi 获取数据并在 WMS 层过滤
     *
     * @param keyword 搜索关键词（商品名称或 SKU ID）
     * @param limit   返回条数上限
     */
    public List<Map<String, Object>> searchSku(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        keyword = keyword.trim();
        List<Map<String, Object>> results = new ArrayList<>();

        // 1. 尝试按 SKU ID 精确匹配
        try {
            Long skuId = Long.parseLong(keyword);
            ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
            if (sku != null) {
                ProductSpuRespDTO spu = productSpuApi.getSpu(sku.getSpuId());
                results.add(buildSkuItem(sku, spu));
                if (results.size() >= limit) return results;
            }
        } catch (NumberFormatException ignored) { }

        // 2. 通过 SPU 名称模糊搜索（调用 ProductSpuApi 新增的 getSpuListByName）
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuListByName(keyword, limit);
        if (spuList != null && !spuList.isEmpty()) {
            Set<Long> spuIds = spuList.stream().map(ProductSpuRespDTO::getId).collect(Collectors.toSet());
            // 查询这些 SPU 下的所有 SKU
            List<ProductSkuRespDTO> skuList = productSkuApi.getSkuListBySpuId(spuIds);
            if (skuList != null) {
                Map<Long, ProductSpuRespDTO> spuMap = spuList.stream()
                        .collect(Collectors.toMap(ProductSpuRespDTO::getId, s -> s, (a, b) -> a));
                for (ProductSkuRespDTO sku : skuList) {
                    ProductSpuRespDTO spu = spuMap.get(sku.getSpuId());
                    results.add(buildSkuItem(sku, spu));
                    if (results.size() >= limit) break;
                }
            }
        }
        return results;
    }

    /**
     * 根据 SKU ID 批量查询并构建搜索结果
     */
    public List<Map<String, Object>> getSkuListByIds(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return Collections.emptyList();

        List<ProductSkuRespDTO> skuList = productSkuApi.getSkuList(skuIds);
        if (skuList.isEmpty()) return Collections.emptyList();

        // 收集 SPU ID
        Set<Long> spuIds = skuList.stream()
                .map(ProductSkuRespDTO::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, ProductSpuRespDTO> spuMap = new HashMap<>();
        if (!spuIds.isEmpty()) {
            List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(spuIds);
            spuMap = spuList.stream()
                    .collect(Collectors.toMap(ProductSpuRespDTO::getId, s -> s, (a, b) -> a));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (ProductSkuRespDTO sku : skuList) {
            ProductSpuRespDTO spu = spuMap.get(sku.getSpuId());
            results.add(buildSkuItem(sku, spu));
        }
        return results;
    }

    /**
     * 构建下拉选项数据
     */
    private Map<String, Object> buildSkuItem(ProductSkuRespDTO sku, ProductSpuRespDTO spu) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", sku.getId());
        item.put("spuId", sku.getSpuId());
        item.put("skuId", sku.getId());
        item.put("spuName", spu != null ? spu.getName() : "未知商品");
        item.put("barCode", sku.getBarCode());
        item.put("price", sku.getPrice());
        // 安全库存上下限：供实时库存查询页判定缺货/积压预警
        item.put("minStock", sku.getMinStock());
        item.put("maxStock", sku.getMaxStock());
        // 展示文本：商品名称 + SKU 信息
        StringBuilder display = new StringBuilder();
        display.append(spu != null ? spu.getName() : "SKU(" + sku.getId() + ")");
        if (sku.getBarCode() != null) {
            display.append(" [").append(sku.getBarCode()).append("]");
        }
        item.put("displayName", display.toString());
        return item;
    }

}