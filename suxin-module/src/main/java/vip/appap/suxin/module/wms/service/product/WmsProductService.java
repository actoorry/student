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
 * WMS 产品服务（通过 ProductApi 跨模块调用）
 *
 * 规范：
 * 1. 禁止直接引入 product 模块的 ServiceImpl、DO 实体
 * 2. 只能使用 ProductSkuApi / ProductSpuApi 对外接口
 * 3. 只读不写：仅调用查询接口，不调用 updateSkuStock 等写操作
 * 4. sku_id 为 FK → product_sku.id，不可在 WMS 中修改 product 侧数据
 */
@Service
@Validated
public class WmsProductService {

    @Resource
    private ProductSkuApi productSkuApi;

    @Resource
    private ProductSpuApi productSpuApi;

    /**
     * 根据 SKU ID 获取产品信息
     */
    public ProductSkuRespDTO getSku(Long skuId) {
        if (skuId == null) return null;
        return productSkuApi.getSku(skuId);
    }

    /**
     * 批量获取 SKU 列表
     */
    public List<ProductSkuRespDTO> getSkuList(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return Collections.emptyList();
        return productSkuApi.getSkuList(skuIds);
    }

    /**
     * 获取 SKU 名称 Map（用于列表展示 skuId → skuName）
     * SKU 本身没有 name 字段，需通过 spuId 获取 SPU 名称
     */
    public Map<Long, String> getSkuNameMap(Collection<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return Collections.emptyMap();
        List<ProductSkuRespDTO> skuList = productSkuApi.getSkuList(skuIds);
        if (skuList.isEmpty()) return Collections.emptyMap();
        // 收集 SPU ID 列表
        Set<Long> spuIds = skuList.stream()
                .map(ProductSkuRespDTO::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 批量查询 SPU 名称
        Map<Long, String> spuNameMap = new HashMap<>();
        if (!spuIds.isEmpty()) {
            List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(spuIds);
            spuNameMap = spuList.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(ProductSpuRespDTO::getId, ProductSpuRespDTO::getName, (a, b) -> a));
        }
        // 构建 SKU ID → 名称映射
        Map<Long, String> result = new HashMap<>();
        for (ProductSkuRespDTO sku : skuList) {
            String name = spuNameMap.get(sku.getSpuId());
            result.put(sku.getId(), name != null ? name : "SKU(" + sku.getId() + ")");
        }
        return result;
    }

    /**
     * 获取单个 SKU 显示名称
     */
    public String getSkuDisplayName(Long skuId) {
        if (skuId == null) return null;
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) return null;
        // 通过 SPU 获取商品名称
        ProductSpuRespDTO spu = productSpuApi.getSpu(sku.getSpuId());
        return spu != null ? spu.getName() : "SKU(" + skuId + ")";
    }

    /**
     * 根据 SKU ID 获取库存管控方式（冗余自 product_spu.stock_mode）
     *
     * @param skuId SKU ID
     * @return 库存管控方式：0=不管理 1=批次管理 2=序列号管理；SKU/SPU 不存在时返回 null
     */
    public Integer getStockModeBySkuId(Long skuId) {
        ProductSkuRespDTO sku = getSku(skuId);
        if (sku == null || sku.getSpuId() == null) {
            return null;
        }
        ProductSpuRespDTO spu = productSpuApi.getSpu(sku.getSpuId());
        return spu != null ? spu.getStockMode() : null;
    }

}