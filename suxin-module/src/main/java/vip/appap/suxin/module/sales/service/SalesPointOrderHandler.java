package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.api.SalesPointActivityApi;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.ORDER_CREATE_FAIL_INSUFFICIENT_USER_POINTS;

/**
 * 积分商城活动订单的 {@link SalesOrderHandler} 实现类
 *
 * @author HUIHUI
 */
@Component
public class SalesPointOrderHandler implements SalesOrderHandler {

    @Resource
    private SalesPointActivityApi pointActivityApi;
    @Resource
    private PartnerApi PartnerApi;

    @Override
    public void beforeOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isPoint(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "积分商城活动兑换商品兑换时，只允许选择一个商品");
        // 校验用户剩余积分是否足够兑换商品
        PartnerRespDTO user = PartnerApi.getUser(order.getUserId());
        if (user.getPoint() < order.getUsePoint()) {
            throw exception(ORDER_CREATE_FAIL_INSUFFICIENT_USER_POINTS);
        }

        // 扣减积分商城活动的库存
        pointActivityApi.updatePointStockDecr(order.getPointActivityId(),
                orderItems.get(0).getSkuId(), orderItems.get(0).getCount());

        // 如果支付金额为 0，则直接设置为已支付
        if (Objects.equals(order.getPayPrice(), 0)) {
            order.setPayStatus(true).setStatus(SalesOrderStatusEnum.UNDELIVERED.getStatus());
        }
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!SalesOrderTypeEnum.isPoint(order.getType())) {
            return;
        }
        // 明确校验一下
        Assert.isTrue(orderItems.size() == 1, "积分商城活动兑换商品兑换时，只允许选择一个商品");

        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        afterCancelOrderItem(order, orderItems.get(0));
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        if (!SalesOrderTypeEnum.isPoint(order.getType())) {
            return;
        }
        // 恢复积分商城活动的库存
        pointActivityApi.updatePointStockIncr(order.getPointActivityId(),
                orderItem.getSkuId(), orderItem.getCount());
    }

}
