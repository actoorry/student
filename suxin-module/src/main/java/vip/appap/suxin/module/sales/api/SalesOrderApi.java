package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesOrderRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface SalesOrderApi {

    /**
     * 获得订单列表
     *
     * @param ids 订单编号数组
     * @return 订单列表
     */
    List<SalesOrderRespDTO> getOrderList(Collection<Long> ids);

    /**
     * 获得订单
     *
     * @param id 订单编号
     * @return 订单
     */
    SalesOrderRespDTO getOrder(Long id);

    /**
     * 取消支付订单
     *
     * @param userId 用户编号
     * @param orderId 订单编号
     * @param cancelType 取消类型
     */
    void cancelPaidOrder(Long userId, Long orderId, Integer cancelType);

}
