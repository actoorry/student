package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTrendSummaryRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import vip.appap.suxin.module.sales.service.bo.SalesSummaryRespBO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Service 接口
 *
 * @author owen
 */
public interface SalesStatisticsService {

    /**
     * 获得交易状况统计对照
     *
     * @return 统计数据对照
     */
    SalesStatisticsDataComparisonRespVO<SalesTrendSummaryRespVO> getTradeStatisticsAnalyse(
            LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易状况统计
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据对照
     */
    Integer getExpensePrice(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得交易状况明细
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据列表
     */
    List<SalesStatisticsDO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 统计指定天数的交易数据
     *
     * @return 统计结果
     */
    String statisticsTrade(Integer days);

    /**
     * 统计指定日期的交易数据
     *
     * @param days 增加的天数
     * @return 交易数据
     */
    SalesSummaryRespBO getTradeSummaryByDays(int days);

    /**
     * 统计指定月份的交易数据
     *
     * @param months 增加的月数
     * @return 交易数据
     */
    SalesSummaryRespBO getTradeSummaryByMonths(int months);

}
