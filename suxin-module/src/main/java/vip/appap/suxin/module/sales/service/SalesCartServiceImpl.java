package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartAddReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartListRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartResetReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartUpdateCountReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCartUpdateSelectedReqVO;
import vip.appap.suxin.module.sales.convert.SalesCartConvert;
import vip.appap.suxin.module.sales.dal.mysql.SalesCartMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_NOT_EXISTS;
import static vip.appap.suxin.module.product.enums.ErrorCodeConstants.SKU_STOCK_NOT_ENOUGH;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.CART_ITEM_NOT_FOUND;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.CART_WECHAT_VIRTUAL_GOODS_NOT_ALLOWED;

/**
 * 销售购物车 Service 实现。
 */
@Service
@Validated
public class SalesCartServiceImpl implements SalesCartService {

    @Resource
    private SalesCartMapper salesCartMapper;

    @Resource
    private ProductSpuApi productSpuApi;

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public Long addCart(Long userId, AppSalesCartAddReqVO addReqVO) {
        SalesCartDO cart = salesCartMapper.selectByUserIdAndSkuId(userId, addReqVO.getSkuId());
        int targetCount = addReqVO.getCount() + (cart == null ? 0 : cart.getCount());
        ProductSkuRespDTO sku = validateProductSku(addReqVO.getSkuId(), targetCount);
        validateSpuCanEnterCart(sku.getSpuId());
        if (cart != null) {
            salesCartMapper.updateById(new SalesCartDO()
                    .setId(cart.getId())
                    .setSelected(true)
                    .setCount(targetCount));
            return cart.getId();
        }
        cart = new SalesCartDO()
                .setUserId(userId)
                .setSelected(true)
                .setSpuId(sku.getSpuId())
                .setSkuId(sku.getId())
                .setCount(addReqVO.getCount());
        salesCartMapper.insert(cart);
        return cart.getId();
    }

    @Override
    public void updateCartCount(Long userId, AppSalesCartUpdateCountReqVO updateReqVO) {
        SalesCartDO cart = validateCartExists(userId, updateReqVO.getId());
        ProductSkuRespDTO sku = validateProductSku(cart.getSkuId(), updateReqVO.getCount());
        validateSpuCanEnterCart(sku.getSpuId());
        salesCartMapper.updateById(new SalesCartDO()
                .setId(cart.getId())
                .setCount(updateReqVO.getCount()));
    }

    @Override
    public void updateCartSelected(Long userId, AppSalesCartUpdateSelectedReqVO updateReqVO) {
        if (Boolean.TRUE.equals(updateReqVO.getSelected())) {
            List<SalesCartDO> carts = salesCartMapper.selectListByIdsAndUserId(updateReqVO.getIds(), userId);
            List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(convertSet(carts, SalesCartDO::getSpuId));
            if (spus.stream().anyMatch(spu -> Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods()))) {
                throw exception(CART_WECHAT_VIRTUAL_GOODS_NOT_ALLOWED);
            }
        }
        salesCartMapper.updateByIdsAndUserId(updateReqVO.getIds(), userId,
                new SalesCartDO().setSelected(updateReqVO.getSelected()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetCart(Long userId, AppSalesCartResetReqVO resetReqVO) {
        SalesCartDO oldCart = validateCartExists(userId, resetReqVO.getId());
        ProductSkuRespDTO targetSku = validateProductSku(resetReqVO.getSkuId(), resetReqVO.getCount());
        validateSpuCanEnterCart(targetSku.getSpuId());
        salesCartMapper.deleteById(oldCart.getId());

        SalesCartDO targetCart = salesCartMapper.selectByUserIdAndSkuId(userId, resetReqVO.getSkuId());
        if (targetCart != null) {
            updateCartCount(userId, new AppSalesCartUpdateCountReqVO()
                    .setId(targetCart.getId())
                    .setCount(resetReqVO.getCount()));
            return;
        }
        addCart(userId, new AppSalesCartAddReqVO()
                .setSkuId(resetReqVO.getSkuId())
                .setCount(resetReqVO.getCount()));
    }

    @Override
    public void deleteCart(Long userId, Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<SalesCartDO> carts = salesCartMapper.selectListByIdsAndUserId(ids, userId);
        if (CollUtil.isEmpty(carts)) {
            return;
        }
        salesCartMapper.deleteByIds(convertSet(carts, SalesCartDO::getId));
    }

    @Override
    public Integer getCartCount(Long userId) {
        return salesCartMapper.selectSelectedCountByUserId(userId);
    }

    @Override
    public AppSalesCartListRespVO getCartList(Long userId) {
        List<SalesCartDO> carts = salesCartMapper.selectListByUserId(userId);
        carts.sort(Comparator.comparing(SalesCartDO::getId).reversed());
        if (CollUtil.isEmpty(carts)) {
            return new AppSalesCartListRespVO()
                    .setValidList(emptyList())
                    .setInvalidList(emptyList());
        }

        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(convertSet(carts, SalesCartDO::getSpuId));
        List<ProductSkuRespDTO> skus = productSkuApi.getSkuList(convertSet(carts, SalesCartDO::getSkuId));
        deleteCartIfSpuDeleted(carts, spus);
        return SalesCartConvert.INSTANCE.convertList(carts, spus, skus);
    }

    @Override
    public List<SalesCartDO> getCartList(Long userId, Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return salesCartMapper.selectListByUserIdAndIds(userId, ids);
    }

    private SalesCartDO validateCartExists(Long userId, Long id) {
        SalesCartDO cart = salesCartMapper.selectByIdAndUserId(id, userId);
        if (cart == null) {
            throw exception(CART_ITEM_NOT_FOUND);
        }
        return cart;
    }

    private void deleteCartIfSpuDeleted(List<SalesCartDO> carts, List<ProductSpuRespDTO> spus) {
        carts.removeIf(cart -> {
            if (spus.stream().noneMatch(spu -> spu.getId().equals(cart.getSpuId()))) {
                salesCartMapper.deleteById(cart.getId());
                return true;
            }
            return false;
        });
    }

    private ProductSkuRespDTO validateProductSku(Long skuId, Integer count) {
        ProductSkuRespDTO sku = productSkuApi.getSku(skuId);
        if (sku == null) {
            throw exception(SKU_NOT_EXISTS);
        }
        if (count > sku.getStock()) {
            throw exception(SKU_STOCK_NOT_ENOUGH);
        }
        return sku;
    }

    private void validateSpuCanEnterCart(Long spuId) {
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu != null && Boolean.TRUE.equals(spu.getIsWechatMiniappVirtualGoods())) {
            throw exception(CART_WECHAT_VIRTUAL_GOODS_NOT_ALLOWED);
        }
    }

}
