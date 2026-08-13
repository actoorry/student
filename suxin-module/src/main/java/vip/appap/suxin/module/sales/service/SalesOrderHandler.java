package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesOrderItemAfterSaleStatusEnum;

import java.util.List;

/**
 * 订单活动特殊逻辑处理器 handler 接口
 * 提供订单生命周期钩子接口；订单创建前、订单创建后、订单支付后、订单取消
 *
 * @author HUIHUI
 */
public interface SalesOrderHandler {

    /**
     * 订单创建前
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void beforeOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {}

    /**
     * 订单创建后
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void afterOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {}

    /**
     * 支付订单后
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void afterPayOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {}

    /**
     * 订单取消后
     *
     * @param order 订单
     * @param orderItems 订单项
     */
    default void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {}

    /**
     * 订单项取消后
     *
     * @param order 订单
     * @param orderItem 订单项
     */
    default void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {}

    /**
     * 订单发货前
     *
     * @param order 订单
     */
    default void beforeDeliveryOrder(SalesOrderDO order) {}

    /**
     * 订单发货后
     *
     * @param order 订单
     */
    default void afterDeliveryOrder(SalesOrderDO order) {}

    /**
     * 订单收货前
     *
     * @param order 订单
     */
    default void beforeReceiveOrder(SalesOrderDO order) {}

    /**
     * 订单收货后
     *
     * @param order 订单
     */
    default void afterReceiveOrder(SalesOrderDO order) {}

    // ========== 公用方法 ==========

    /**
     * 过滤"未售后"的订单项列表
     *
     * @param orderItems 订单项列表
     * @return 过滤后的订单项列表
     */
    default List<SalesOrderItemDO> filterOrderItemListByNoneAfterSale(List<SalesOrderItemDO> orderItems) {
        return CollectionUtils.filterList(orderItems,
                item -> SalesOrderItemAfterSaleStatusEnum.isNone(item.getAfterSaleStatus()));
    }

}
