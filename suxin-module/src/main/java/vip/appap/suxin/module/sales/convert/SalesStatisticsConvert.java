package vip.appap.suxin.module.sales.convert;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesOrderCountRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesSummaryRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTrendSummaryExcelVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesTrendSummaryRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import vip.appap.suxin.module.sales.service.bo.SalesAfterSaleSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesOrderSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesSummaryRespBO;
import vip.appap.suxin.module.sales.service.bo.SalesTradeStatisticsWalletSummaryRespBO;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易统计 Convert
 *
 * @author owen
 */
@Mapper
public interface SalesStatisticsConvert {

    SalesStatisticsConvert INSTANCE = Mappers.getMapper(SalesStatisticsConvert.class);

    default SalesStatisticsDataComparisonRespVO<SalesSummaryRespVO> convert(SalesSummaryRespBO yesterdayData,
                                                             SalesSummaryRespBO beforeYesterdayData,
                                                             SalesSummaryRespBO monthData,
                                                             SalesSummaryRespBO lastMonthData) {
        return convert(convert(yesterdayData, monthData), convert(beforeYesterdayData, lastMonthData));
    }


    default SalesSummaryRespVO convert(SalesSummaryRespBO yesterdayData, SalesSummaryRespBO monthData) {
        return new SalesSummaryRespVO()
                .setYesterdayOrderCount(yesterdayData.getCount()).setYesterdayPayPrice(yesterdayData.getSummary())
                .setMonthOrderCount(monthData.getCount()).setMonthPayPrice(monthData.getSummary());
    }

    SalesStatisticsDataComparisonRespVO<SalesSummaryRespVO> convert(SalesSummaryRespVO value, SalesSummaryRespVO reference);

    SalesStatisticsDataComparisonRespVO<SalesTrendSummaryRespVO> convert(SalesTrendSummaryRespVO value,
                                                          SalesTrendSummaryRespVO reference);

    List<SalesTrendSummaryExcelVO> convertList02(List<SalesTrendSummaryRespVO> list);

    SalesStatisticsDO convert(LocalDateTime time, SalesOrderSummaryRespBO orderSummary,
                              SalesAfterSaleSummaryRespBO afterSaleSummary, Integer brokerageSettlementPrice,
                              SalesTradeStatisticsWalletSummaryRespBO walletSummary);

    @IterableMapping(qualifiedByName = "convert")
    List<SalesTrendSummaryRespVO> convertList(List<SalesStatisticsDO> list);

    SalesTrendSummaryRespVO convertA(SalesStatisticsDO tradeStatistics);

    @Named("convert")
    default SalesTrendSummaryRespVO convert(SalesStatisticsDO tradeStatistics) {
        SalesTrendSummaryRespVO vo = convertA(tradeStatistics);
        return vo
                .setDate(tradeStatistics.getTime().toLocalDate())
                // 营业额 = 商品支付金额 + 充值金额
                .setTurnoverPrice(tradeStatistics.getOrderPayPrice() + tradeStatistics.getRechargePayPrice())
                // 支出金额 = 余额支付金额 + 支付佣金金额 + 商品退款金额
                .setExpensePrice(tradeStatistics.getWalletPayPrice() + tradeStatistics.getBrokerageSettlementPrice() + tradeStatistics.getAfterSaleRefundPrice());
    }

    SalesOrderCountRespVO convert(Long undelivered, Long pickUp, Long afterSaleApply, Long auditingWithdraw);

}
