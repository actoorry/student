package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayRefundStatusEnum;
import vip.appap.suxin.module.sales.dal.mysql.SalesPayStatisticsWalletMapper;
import vip.appap.suxin.module.sales.service.bo.SalesPayStatisticsRechargeSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesTradeStatisticsWalletSummaryRespBO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 钱包的统计 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class SalesPayStatisticsWalletServiceImpl implements SalesPayStatisticsWalletService {

    @Resource
    private SalesPayStatisticsWalletMapper payWalletStatisticsMapper;

    @Override
    public SalesTradeStatisticsWalletSummaryRespBO getWalletSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        SalesTradeStatisticsWalletSummaryRespBO paySummary = payWalletStatisticsMapper.selectRechargeSummaryByPayTimeBetween(
                beginTime, endTime, true);
        SalesTradeStatisticsWalletSummaryRespBO refundSummary = payWalletStatisticsMapper.selectRechargeSummaryByRefundTimeBetween(
                beginTime, endTime, PayRefundStatusEnum.SUCCESS.getStatus());
        Integer walletPayPrice = payWalletStatisticsMapper.selectPriceSummaryByBizTypeAndCreateTimeBetween(
                beginTime, endTime, AccountBizTypeEnum.PAYMENT.getType());
        // 拼接
        paySummary.setWalletPayPrice(walletPayPrice)
                .setRechargeRefundCount(refundSummary.getRechargeRefundCount())
                .setRechargeRefundPrice(refundSummary.getRechargeRefundPrice());
        return paySummary;
    }

    @Override
    public SalesPayStatisticsRechargeSummaryRespBO getUserRechargeSummary(LocalDateTime beginTime, LocalDateTime endTime) {
        return payWalletStatisticsMapper.selectRechargeSummaryGroupByWalletId(beginTime, endTime, true);
    }

    @Override
    public Integer getRechargePriceSummary() {
        return payWalletStatisticsMapper.selectRechargePriceSummary(Boolean.TRUE);
    }

}

