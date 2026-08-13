package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.sales.convert.SalesOrderConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * 商品 SKU 库存的 {@link SalesOrderHandler} 实现类
 *
 * @author 书心软件
 */
@Component
public class SalesProductSkuOrderHandler implements SalesOrderHandler {

    @Resource
    private ProductSkuApi productSkuApi;

    @Override
    public void beforeOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        productSkuApi.updateSkuStock(SalesOrderConvert.INSTANCE.convertNegative(orderItems));
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        productSkuApi.updateSkuStock(SalesOrderConvert.INSTANCE.convert(orderItems));
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        productSkuApi.updateSkuStock(SalesOrderConvert.INSTANCE.convert(singletonList(orderItem)));
    }

}
