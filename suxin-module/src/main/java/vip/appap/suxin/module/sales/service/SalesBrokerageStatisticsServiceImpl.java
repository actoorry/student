package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.dal.mysql.SalesBrokerageStatisticsMapper;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 分销统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesBrokerageStatisticsServiceImpl implements SalesBrokerageStatisticsService {

    @Resource
    private SalesBrokerageStatisticsMapper brokerageStatisticsMapper;

    @Override
    public Integer getBrokerageSettlementPriceSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return brokerageStatisticsMapper.selectSummaryPriceByStatusAndUnfreezeTimeBetween(
                SalesBrokerageRecordBizTypeEnum.ORDER.getType(), SalesBrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                beginTime, endTime);
    }

    @Override
    public Long getWithdrawCountByStatus(SalesBrokerageWithdrawStatusEnum status) {
        return brokerageStatisticsMapper.selectWithdrawCountByStatus(status.getStatus());
    }

}
