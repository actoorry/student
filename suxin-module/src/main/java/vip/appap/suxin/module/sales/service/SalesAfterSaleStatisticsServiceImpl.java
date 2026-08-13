package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.dal.mysql.SalesAfterSaleStatisticsMapper;
import vip.appap.suxin.module.sales.service.bo.SalesAfterSaleSummaryRespBO;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 售后统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesAfterSaleStatisticsServiceImpl implements SalesAfterSaleStatisticsService {

    @Resource
    private SalesAfterSaleStatisticsMapper afterSaleStatisticsMapper;

    @Override
    public SalesAfterSaleSummaryRespBO getAfterSaleSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return afterSaleStatisticsMapper.selectSummaryByRefundTimeBetween(beginTime, endTime);
    }

    @Override
    public Long getCountByStatus(SalesAfterSaleStatusEnum status) {
        return afterSaleStatisticsMapper.selectCountByStatus(status.getStatus());
    }

}
