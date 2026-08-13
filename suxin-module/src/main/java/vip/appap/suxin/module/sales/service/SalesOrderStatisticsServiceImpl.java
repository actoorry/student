package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTradeOrderSummaryRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderTrendReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderTrendRespVO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderStatisticsMapper;
import vip.appap.suxin.module.sales.enums.SalesStatisticsTimeRangeTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPartnerStatisticsAreaStatisticsRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 交易订单统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesOrderStatisticsServiceImpl implements SalesOrderStatisticsService {

    @Resource
    private SalesOrderStatisticsMapper tradeOrderStatisticsMapper;

    @Override
    public SalesOrderSummaryRespBO getOrderSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return new SalesOrderSummaryRespBO()
                .setOrderCreateCount(tradeOrderStatisticsMapper.selectCountByCreateTimeBetween(beginTime, endTime))
                .setOrderPayCount(tradeOrderStatisticsMapper.selectCountByPayTimeBetween(beginTime, endTime))
                .setOrderPayPrice(tradeOrderStatisticsMapper.selectSummaryPriceByPayTimeBetween(beginTime, endTime));
    }

    @Override
    public List<SalesPartnerStatisticsAreaStatisticsRespBO> getSummaryListByAreaId() {
        return tradeOrderStatisticsMapper.selectSummaryListByAreaId();
    }

    @Override
    public Integer getOrderUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectUserCountByCreateTimeBetween(beginTime, endTime);
    }

    @Override
    public Integer getPayUserCount(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectUserCountByPayTimeBetween(beginTime, endTime);
    }

    @Override
    public Integer getOrderPayPrice(LocalDateTime beginTime, LocalDateTime endTime) {
        return tradeOrderStatisticsMapper.selectSummaryPriceByPayTimeBetween(beginTime, endTime);
    }

    @Override
    public Long getCountByStatusAndDeliveryType(Integer status, Integer deliveryType) {
        return tradeOrderStatisticsMapper.selectCountByStatusAndDeliveryType(status, deliveryType);
    }

    @Override
    public SalesStatisticsDataComparisonRespVO<SalesTradeOrderSummaryRespVO> getOrderComparison() {
        return new SalesStatisticsDataComparisonRespVO<SalesTradeOrderSummaryRespVO>()
                .setValue(getPayPriceSummary(LocalDateTime.now()))
                .setReference(getPayPriceSummary(LocalDateTime.now().minusDays(1)));
    }

    private SalesTradeOrderSummaryRespVO getPayPriceSummary(LocalDateTime date) {
        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(date);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(date);
        return tradeOrderStatisticsMapper.selectPaySummaryByPayStatusAndPayTimeBetween(
                Boolean.TRUE, beginTime, endTime);
    }

    @Override
    public List<SalesStatisticsDataComparisonRespVO<SalesOrderTrendRespVO>> getOrderCountTrendComparison(SalesOrderTrendReqVO reqVO) {
        // 查询当前数据
        List<SalesOrderTrendRespVO> value = getOrderCountTrend(reqVO.getType(), reqVO.getBeginTime(), reqVO.getEndTime());
        // 查询对照数据
        LocalDateTime referenceEndTime = reqVO.getBeginTime().minusDays(1);
        LocalDateTime referenceBeginTime = referenceEndTime.minus(Duration.between(reqVO.getBeginTime(), reqVO.getEndTime()));
        List<SalesOrderTrendRespVO> reference = getOrderCountTrend(reqVO.getType(), referenceBeginTime, referenceEndTime);
        // 顺序对比返回
        return IntStream.range(0, value.size())
                .mapToObj(index -> new SalesStatisticsDataComparisonRespVO<SalesOrderTrendRespVO>()
                        .setValue(CollUtil.get(value, index))
                        .setReference(CollUtil.get(reference, index)))
                .collect(Collectors.toList());
    }

    private List<SalesOrderTrendRespVO> getOrderCountTrend(Integer timeRangeType, LocalDateTime beginTime, LocalDateTime endTime) {
        // 情况一：按年统计时，以月份分组
        if (SalesStatisticsTimeRangeTypeEnum.YEAR.getType().equals(timeRangeType)) {
            return tradeOrderStatisticsMapper.selectListByPayTimeBetweenAndGroupByMonth(beginTime, endTime);
        }
        // 情况二：其它以天分组（天、周、月）
        return tradeOrderStatisticsMapper.selectListByPayTimeBetweenAndGroupByDay(beginTime, endTime);
    }

}
