package vip.appap.suxin.module.product.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSkuUpdateStockReqDTO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.service.ProductSkuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 商品 SKU API 实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSkuApiImpl implements ProductSkuApi {

    @Resource
    private ProductSkuService productSkuService;

    @Override
    public ProductSkuRespDTO getSku(Long id) {
        ProductSkuDO sku = productSkuService.getSku(id);
        return BeanUtils.toBean(sku, ProductSkuRespDTO.class);
    }

    @Override
    public List<ProductSkuRespDTO> getSkuList(Collection<Long> ids) {
        List<ProductSkuDO> skus = productSkuService.getSkuList(ids);
        return BeanUtils.toBean(skus, ProductSkuRespDTO.class);
    }

    @Override
    public List<ProductSkuRespDTO> getSkuListBySpuId(Collection<Long> spuIds) {
        List<ProductSkuDO> skus = productSkuService.getSkuListBySpuId(spuIds);
        return BeanUtils.toBean(skus, ProductSkuRespDTO.class);
    }

    @Override
    public void updateSkuStock(ProductSkuUpdateStockReqDTO updateStockReqDTO) {
        productSkuService.updateSkuStock(updateStockReqDTO);
    }

}
