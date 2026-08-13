package vip.appap.suxin.module.sales.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTrendSummaryRespVO;
import vip.appap.suxin.module.sales.convert.SalesStatisticsConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesStatisticsMapper;
import vip.appap.suxin.module.sales.service.SalesPayStatisticsWalletService;
import vip.appap.suxin.module.sales.service.bo.SalesAfterSaleSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesTradeStatisticsWalletSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 交易统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesStatisticsServiceImpl implements SalesStatisticsService {

    @Resource
    private SalesStatisticsMapper tradeStatisticsMapper;

    @Resource
    private SalesOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private SalesAfterSaleStatisticsService afterSaleStatisticsService;
    @Resource
    private SalesBrokerageStatisticsService brokerageStatisticsService;
    @Resource
    private SalesPayStatisticsWalletService payWalletStatisticsService;

    @Override
    public SalesSummaryRespBO getTradeSummaryByDays(int days) {
        LocalDateTime date = LocalDateTime.now().plusDays(days);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(
                LocalDateTimeUtil.beginOfDay(date), LocalDateTimeUtil.endOfDay(date));
    }

    @Override
    public SalesSummaryRespBO getTradeSummaryByMonths(int months) {
        LocalDateTime monthDate = LocalDateTime.now().plusMonths(months);
        return tradeStatisticsMapper.selectOrderCreateCountSumAndOrderPayPriceSumByTimeBetween(
                LocalDateTimeUtils.beginOfMonth(monthDate), LocalDateTimeUtils.endOfMonth(monthDate));
    }

    @Override
    public SalesStatisticsDataComparisonRespVO<SalesTrendSummaryRespVO> getTradeStatisticsAnalyse(LocalDateTime beginTime,
                                                                                        LocalDateTime endTime) {
        // 统计数据
        SalesTrendSummaryRespVO value = tradeStatisticsMapper.selectVoByTimeBetween(beginTime, endTime);
        // 对照数据
        LocalDateTime referenceBeginTime = beginTime.minus(Duration.between(beginTime, endTime));
        SalesTrendSummaryRespVO reference = tradeStatisticsMapper.selectVoByTimeBetween(referenceBeginTime, beginTime);
        return SalesStatisticsConvert.INSTANCE.convert(value, reference);
    }

    @Override
    public Integer getExpensePrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectExpensePriceByTimeBetween(beginTime, endTime);
    }

    @Override
    public List<SalesStatisticsDO> getTradeStatisticsList(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeStatisticsMapper.selectListByTimeBetween(beginTime, endTime);
    }

    @Override
    public String statisticsTrade(Integer days) {
        LocalDateTime today = LocalDateTime.now();
        return IntStream.rangeClosed(1, days)
                .mapToObj(day -> statisticsTrade(today.minusDays(day)))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    /**
     * 统计交易数据
     *
     * @param date 需要统计的日期
     * @return 统计结果
     */
    private String statisticsTrade(LocalDateTime date) {
        // 1. 处理统计时间范围
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(date);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(date);
        String dateStr = DatePattern.NORM_DATE_FORMATTER.format(date);
        // 2. 检查该日是否已经统计过
        SalesStatisticsDO entity = tradeStatisticsMapper.selectByTimeBetween(beginTime, endTime);
        if (entity != null) {
            return dateStr + " 数据已存在，如果需要重新统计，请先删除对应的数据";
        }

        // 3. 从各个数据表，统计对应数据
        StopWatch stopWatch = new StopWatch(dateStr);
        // 3.1 统计订单
        stopWatch.start("统计订单");
        SalesOrderSummaryRespBO orderSummary = tradeOrderStatisticsService.getOrderSummary(beginTime, endTime);
        stopWatch.stop();
        // 3.2 统计售后
        stopWatch.start("统计售后");
        SalesAfterSaleSummaryRespBO afterSaleSummary = afterSaleStatisticsService.getAfterSaleSummary(beginTime, endTime);
        stopWatch.stop();
        // 3.3 统计佣金
        stopWatch.start("统计佣金");
        Integer brokerageSettlementPrice = brokerageStatisticsService.getBrokerageSettlementPriceSummary(beginTime, endTime);
        stopWatch.stop();
        // 3.4 统计充值
        stopWatch.start("统计充值");
        SalesTradeStatisticsWalletSummaryRespBO walletSummary = payWalletStatisticsService.getWalletSummary(beginTime, endTime);
        stopWatch.stop();

        // 4. 插入数据
        entity = SalesStatisticsConvert.INSTANCE.convert(date, orderSummary, afterSaleSummary, brokerageSettlementPrice,
                walletSummary);
        tradeStatisticsMapper.insert(entity);
        return stopWatch.prettyPrint();
    }

}
