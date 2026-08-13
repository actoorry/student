package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.api.SalesBargainActivityApi;
import vip.appap.suxin.module.sales.api.SalesBargainRecordApi;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 砍价订单的 {@link SalesOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class SalesBargainOrderHandler implements SalesOrderHandler {

    @Resource
    private SalesBargainActivityApi bargainActivityApi;
    @Resource
    private SalesBargainRecordApi bargainRecordApi;

    @Override
    public void beforeOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "砍价时，只允许选择一个商品");

        // 扣减砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(order.getBargainActivityId(),
                -orderItems.get(0).getCount());
    }

    @Override
    public void afterOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "砍价时，只允许选择一个商品");

        // 记录砍价记录对应的订单编号
        bargainRecordApi.updateBargainRecordOrderId(order.getBargainRecordId(), order.getId());
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "砍价时，只允许选择一个商品");

        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        afterCancelOrderItem(order, orderItems.get(0));
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        if (!SalesOrderTypeEnum.isBargain(order.getType())) {
            return;
        }
        // 恢复（增加）砍价活动的库存
        bargainActivityApi.updateBargainActivityStock(order.getBargainActivityId(), orderItem.getCount());
    }

}
