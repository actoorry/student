package vip.appap.suxin.module.sales.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTradeOrderSummaryRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderTrendRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import vip.appap.suxin.module.sales.service.bo.SalesPartnerStatisticsAreaStatisticsRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface SalesOrderStatisticsMapper extends BaseMapperX<SalesStatisticsDO> {

    List<SalesPartnerStatisticsAreaStatisticsRespBO> selectSummaryListByAreaId();

    Integer selectCountByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                           @Param("endTime") LocalDateTime endTime);

    Integer selectCountByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                        @Param("endTime") LocalDateTime endTime);

    Integer selectSummaryPriceByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime);

    Integer selectUserCountByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime);

    Integer selectUserCountByPayTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 按照支付时间统计订单（按天分组）
     *
     * @param beginTime 支付起始时间
     * @param endTime   支付截止时间
     * @return 订单统计列表
     */
    List<SalesOrderTrendRespVO> selectListByPayTimeBetweenAndGroupByDay(@Param("beginTime") LocalDateTime beginTime,
                                                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 按照支付时间统计订单（按月分组）
     *
     * @param beginTime 支付起始时间
     * @param endTime   支付截止时间
     * @return 订单统计列表
     */
    List<SalesOrderTrendRespVO> selectListByPayTimeBetweenAndGroupByMonth(@Param("beginTime") LocalDateTime beginTime,
                                                                          @Param("endTime") LocalDateTime endTime);

    Long selectCountByStatusAndDeliveryType(@Param("status") Integer status, @Param("deliveryType") Integer deliveryType);

    SalesTradeOrderSummaryRespVO selectPaySummaryByPayStatusAndPayTimeBetween(@Param("payStatus") Boolean payStatus,
                                                                         @Param("beginTime") LocalDateTime beginTime,
                                                                         @Param("endTime") LocalDateTime endTime);

}
