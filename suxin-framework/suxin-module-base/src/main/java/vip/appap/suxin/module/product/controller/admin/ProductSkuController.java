package vip.appap.suxin.module.product.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.product.controller.admin.vo.ProductSkuSimpleRespVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSkuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductUnitMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

@Tag(name = "管理后台 - 商品 SKU")
@RestController
@RequestMapping("/product/sku")
@Validated
public class ProductSkuController {

    @Resource
    private ProductSkuMapper skuMapper;

    @Resource
    private ProductSpuMapper spuMapper;

    @Resource
    private ProductUnitMapper unitMapper;

    @GetMapping("/simple-list")
    @Operation(summary = "获得 SKU 精简列表", description = "主要用于商机/合同的选产品下拉")
    public CommonResult<List<ProductSkuSimpleRespVO>> getSimpleSkuList() {
        // 1. 查询所有未删除的 SKU
        List<ProductSkuDO> skuList = skuMapper.selectList();
        if (skuList.isEmpty()) {
            return success(List.of());
        }

        // 2. 批量查询关联的 SPU
        Set<Long> spuIds = skuList.stream().map(ProductSkuDO::getSpuId).collect(Collectors.toSet());
        List<ProductSpuDO> spuList = spuMapper.selectByIds(spuIds);
        Map<Long, ProductSpuDO> spuMap = convertMap(spuList, ProductSpuDO::getId);

        // 3. 批量查询关联的单位
        Set<Long> unitIds = spuList.stream().map(ProductSpuDO::getUnitId).filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, ProductUnitDO> unitMap = convertMap(unitMapper.selectByIds(unitIds), ProductUnitDO::getId);

        // 4. 组装返回
        List<ProductSkuSimpleRespVO> result = new ArrayList<>();
        for (ProductSkuDO sku : skuList) {
            ProductSpuDO spu = spuMap.get(sku.getSpuId());
            if (spu == null) {
                continue;
            }
            ProductSkuSimpleRespVO vo = new ProductSkuSimpleRespVO();
            vo.setId(sku.getId());
            vo.setSpuId(sku.getSpuId());
            vo.setSpuName(spu.getName());
            vo.setBarCode(sku.getBarCode());
            vo.setPrice(sku.getPrice());
            vo.setStock(sku.getStock());
            vo.setQuantity(sku.getQuantity());
            vo.setStatus(spu.getStatus());
            vo.setUnitId(spu.getUnitId());
            if (spu.getUnitId() != null) {
                ProductUnitDO unit = unitMap.get(spu.getUnitId());
                if (unit != null) {
                    vo.setUnitName(unit.getName());
                }
            }
            result.add(vo);
        }
        return success(result);
    }

}
