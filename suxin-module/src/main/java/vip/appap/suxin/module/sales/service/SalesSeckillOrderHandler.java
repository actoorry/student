package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.api.SalesSeckillActivityApi;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 秒杀订单的 {@link SalesOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class SalesSeckillOrderHandler implements SalesOrderHandler {

    @Resource
    private SalesSeckillActivityApi seckillActivityApi;

    @Override
    public void beforeOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isSeckill(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "秒杀时，只允许选择一个商品");

        // 扣减秒杀活动的库存
        seckillActivityApi.updateSeckillStockDecr(order.getSeckillActivityId(),
                orderItems.get(0).getSkuId(), orderItems.get(0).getCount());
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isSeckill(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "秒杀时，只允许选择一个商品");

        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        afterCancelOrderItem(order, orderItems.get(0));
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        if (!SalesOrderTypeEnum.isSeckill(order.getType())) {
            return;
        }
        // 恢复秒杀活动的库存
        seckillActivityApi.updateSeckillStockIncr(order.getSeckillActivityId(),
                orderItem.getSkuId(), orderItem.getCount());
    }

}
