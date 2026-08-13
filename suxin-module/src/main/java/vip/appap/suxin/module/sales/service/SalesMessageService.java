package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.service.bo.SalesOrderMessageWhenDeliveryOrderReqBO;

/**
 * Trade 消息 service 接口
 *
 * @author HUIHUI
 */
public interface SalesMessageService {

    /**
     * 订单发货时发送通知
     *
     * @param reqBO 发送消息
     */
    void sendMessageWhenDeliveryOrder(SalesOrderMessageWhenDeliveryOrderReqBO reqBO);

}
