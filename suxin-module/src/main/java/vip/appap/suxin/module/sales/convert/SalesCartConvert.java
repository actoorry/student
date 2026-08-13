package vip.appap.suxin.module.sales.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductSkuBaseRespVO;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductSpuBaseRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartListRespVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

@Mapper
public interface SalesCartConvert {

    SalesCartConvert INSTANCE = Mappers.getMapper(SalesCartConvert.class);

    default AppSalesCartListRespVO convertList(List<SalesCartDO> carts,
                                               List<ProductSpuRespDTO> spus,
                                               List<ProductSkuRespDTO> skus) {
        Map<Long, ProductSpuRespDTO> spuMap = convertMap(spus, ProductSpuRespDTO::getId);
        Map<Long, ProductSkuRespDTO> skuMap = convertMap(skus, ProductSkuRespDTO::getId);
        List<AppSalesCartListRespVO.Cart> validList = new ArrayList<>(carts.size());
        List<AppSalesCartListRespVO.Cart> invalidList = new ArrayList<>();
        carts.forEach(cart -> {
            AppSalesCartListRespVO.Cart cartVO = new AppSalesCartListRespVO.Cart()
                    .setId(cart.getId())
                    .setCount(cart.getCount())
                    .setSelected(cart.getSelected());
            ProductSpuRespDTO spu = spuMap.get(cart.getSpuId());
            ProductSkuRespDTO sku = skuMap.get(cart.getSkuId());
            cartVO.setSpu(BeanUtils.toBean(spu, AppSalesProductSpuBaseRespVO.class))
                    .setSku(BeanUtils.toBean(sku, AppSalesProductSkuBaseRespVO.class));
            if (spu == null || sku == null
                    || !ProductSpuStatusEnum.isEnable(spu.getStatus())
                    || sku.getStock() < cart.getCount()
                    || Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
                if (spu != null && Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
                    cartVO.setSelected(false)
                            .setInvalidReason("商品支付方式已变更，请移除后直接购买");
                }
                invalidList.add(cartVO);
            } else {
                validList.add(cartVO);
            }
        });
        return new AppSalesCartListRespVO()
                .setValidList(validList)
                .setInvalidList(invalidList);
    }

}
