package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.service.bo.SalesPayStatisticsRechargeSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesTradeStatisticsWalletSummaryRespBO;

import java.time.LocalDateTime;

/**
 * 钱包的统计 Service 接口
 *
 * @author owen
 */
public interface SalesPayStatisticsWalletService {

    /**
     * 获取钱包统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 钱包统计
     */
    SalesTradeStatisticsWalletSummaryRespBO getWalletSummary(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取钱包充值统计
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 钱包充值统计
     */
    SalesPayStatisticsRechargeSummaryRespBO getUserRechargeSummary(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取充值金额合计
     *
     * @return 充值金额合计
     */
    Integer getRechargePriceSummary();

}
