package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderLogDO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderLogCreateReqBO;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * 交易下单日志 Service 接口
 *
 * @author 陈賝
 * @since 2023/7/6 15:44
 */
public interface SalesOrderLogService {

    /**
     * 创建交易下单日志
     *
     * @param logDTO 日志记录
     * @author 陈賝
     * @since 2023/7/6 15:45
     */
    @Async
    void createOrderLog(SalesOrderLogCreateReqBO logDTO);

    /**
     * 获得交易订单日志列表
     *
     * @param orderId 订单编号
     * @return 交易订单日志列表
     */
    List<SalesOrderLogDO> getOrderLogListByOrderId(Long orderId);

}
