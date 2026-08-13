package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.rongjh.enums.LoveValueBizTypeEnum;
import vip.appap.suxin.module.rongjh.service.LoveValueService;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.getSumValue;

/**
 * 戎爱心值订单处理器：
 * 1) 普通订单收货后入账；
 * 2) 虚拟服务/会员订单支付即完成，支付后入账；
 * 3) 订单取消、售后退款时回滚。
 */
@Component
public class SalesLoveValueOrderHandler implements SalesOrderHandler {

    @Resource
    private LoveValueService loveValueService;
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public void afterPayOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!isAllVirtual(orderItems)) {
            return;
        }
        Integer effectiveAmountFen = calcEffectiveAmountFen(order.getPayPrice(), order.getRefundPrice());
        loveValueService.addOrderLoveValue(order.getUserId(), order.getId(), effectiveAmountFen,
                LoveValueBizTypeEnum.ORDER_PAY_VIRTUAL.getCode(), "虚拟订单支付完成入账");
    }

    @Override
    public void afterReceiveOrder(SalesOrderDO order) {
        Integer effectiveAmountFen = calcEffectiveAmountFen(order.getPayPrice(), order.getRefundPrice());
        loveValueService.addOrderLoveValue(order.getUserId(), order.getId(), effectiveAmountFen,
                LoveValueBizTypeEnum.ORDER_RECEIVE.getCode(), "订单收货完成入账");
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (!Boolean.TRUE.equals(order.getPayStatus())) {
            return;
        }
        List<SalesOrderItemDO> normalItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(normalItems)) {
            return;
        }
        Integer rollbackAmountFen = getSumValue(normalItems, SalesOrderItemDO::getPayPrice, Integer::sum);
        loveValueService.rollbackOrderLoveValue(order.getUserId(), order.getId(), null, rollbackAmountFen,
                LoveValueBizTypeEnum.ORDER_CANCEL.getCode(), "订单取消回滚爱心值");
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        Integer rollbackAmountFen = orderItem.getPayPrice() == null ? 0 : orderItem.getPayPrice();
        loveValueService.rollbackOrderLoveValue(order.getUserId(), order.getId(), orderItem.getId(), rollbackAmountFen,
                LoveValueBizTypeEnum.ORDER_ITEM_REFUND.getCode(), "订单项售后退款回滚爱心值");
    }

    private Integer calcEffectiveAmountFen(Integer payPrice, Integer refundPrice) {
        if (payPrice == null || payPrice <= 0) {
            return 0;
        }
        int refunded = refundPrice == null ? 0 : refundPrice;
        return Math.max(payPrice - refunded, 0);
    }

    private boolean isAllVirtual(List<SalesOrderItemDO> orderItems) {
        if (CollUtil.isEmpty(orderItems)) {
            return false;
        }
        return orderItems.stream().allMatch(orderItem -> {
            ProductSpuRespDTO spu = productSpuApi.getSpu(orderItem.getSpuId());
            return spu != null && ProductTypeEnum.isVirtualLike(spu.getType());
        });
    }
}
