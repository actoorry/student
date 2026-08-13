package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.service.bo.SalesPartnerStatisticsAreaStatisticsRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderSummaryRespBO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单的统计 Service 接口
 *
 * @author owen
 */
public interface SalesOrderStatisticsService {

    /**
     * 获取订单统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 订单统计结果
     */
    SalesOrderSummaryRespBO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取地区订单统计
     *
     * @return 订单统计结果
     */
    List<SalesPartnerStatisticsAreaStatisticsRespBO> getSummaryListByAreaId();

    /**
     * 获取下单用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 下单用户数量
     */
    Integer getOrderUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取支付用户数量
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 支付用户数量
     */
    Integer getPayUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取支付金额
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 支付用户金额
     */
    Integer getOrderPayPrice(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 根据订单状态、物流类型，获得交易订单数量
     *
     * @return 订单数量
     */
    Long getCountByStatusAndDeliveryType(Integer status, Integer deliveryType);

    /**
     * 交易订单销售额对照
     *
     * @return 销售额对照
     */
    SalesStatisticsDataComparisonRespVO<SalesTradeOrderSummaryRespVO> getOrderComparison();

    /**
     * 获得订单量趋势统计
     *
     * @param reqVO 统计参数
     * @return 订单量趋势统计
     */
    List<SalesStatisticsDataComparisonRespVO<SalesOrderTrendRespVO>> getOrderCountTrendComparison(SalesOrderTrendReqVO reqVO);

}
