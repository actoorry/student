package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.util.ArrayUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesStatisticsDataComparisonRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesStatisticsConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesStatisticsDO;
import vip.appap.suxin.module.sales.service.SalesAfterSaleStatisticsService;
import vip.appap.suxin.module.sales.service.SalesBrokerageStatisticsService;
import vip.appap.suxin.module.sales.service.SalesOrderStatisticsService;
import vip.appap.suxin.module.sales.service.SalesStatisticsService;
import vip.appap.suxin.module.sales.service.bo.SalesSummaryRespBO;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 交易统计")
@RestController
@RequestMapping("/sales/statistics/trade")
@Validated
@Slf4j
public class SalesStatisticsController {

    @Resource
    private SalesStatisticsService tradeStatisticsService;
    @Resource
    private SalesOrderStatisticsService tradeOrderStatisticsService;
    @Resource
    private SalesAfterSaleStatisticsService afterSaleStatisticsService;
    @Resource
    private SalesBrokerageStatisticsService brokerageStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获得交易统计")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:query')")
    public CommonResult<SalesStatisticsDataComparisonRespVO<SalesSummaryRespVO>> getTradeSummaryComparison() {
        // 1.1 昨天的数据
        SalesSummaryRespBO yesterdayData = tradeStatisticsService.getTradeSummaryByDays(-1);
        // 1.2 前天的数据（用于对照昨天的数据）
        SalesSummaryRespBO beforeYesterdayData = tradeStatisticsService.getTradeSummaryByDays(-2);

        // 2.1 本月数据
        SalesSummaryRespBO monthData = tradeStatisticsService.getTradeSummaryByMonths(0);
        // 2.2 上月数据（用于对照本月的数据）
        SalesSummaryRespBO lastMonthData = tradeStatisticsService.getTradeSummaryByMonths(-1);
        // 拼接数据
        return success(SalesStatisticsConvert.INSTANCE.convert(yesterdayData, beforeYesterdayData, monthData, lastMonthData));
    }

    @GetMapping("/analyse")
    @Operation(summary = "获得交易状况统计")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:query')")
    public CommonResult<SalesStatisticsDataComparisonRespVO<SalesTrendSummaryRespVO>> getTradeStatisticsAnalyse(SalesTrendReqVO reqVO) {
        return success(tradeStatisticsService.getTradeStatisticsAnalyse(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1)));
    }

    @GetMapping("/list")
    @Operation(summary = "获得交易状况明细")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:query')")
    public CommonResult<List<SalesTrendSummaryRespVO>> getTradeStatisticsList(SalesTrendReqVO reqVO) {
        List<SalesStatisticsDO> list = tradeStatisticsService.getTradeStatisticsList(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1));
        return success(SalesStatisticsConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出获得交易状况明细 Excel")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:export')")
    public void exportTradeStatisticsExcel(SalesTrendReqVO reqVO, HttpServletResponse response) throws IOException {
        List<SalesStatisticsDO> list = tradeStatisticsService.getTradeStatisticsList(ArrayUtil.get(reqVO.getTimes(), 0),
                ArrayUtil.get(reqVO.getTimes(), 1));
        // 导出 Excel
        List<SalesTrendSummaryRespVO> voList = SalesStatisticsConvert.INSTANCE.convertList(list);
        List<SalesTrendSummaryExcelVO> data = SalesStatisticsConvert.INSTANCE.convertList02(voList);
        ExcelUtils.write(response, "交易状况.xls", "数据", SalesTrendSummaryExcelVO.class, data);
    }

    @GetMapping("/order-count")
    @Operation(summary = "获得交易订单数量")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:query')")
    public CommonResult<SalesOrderCountRespVO> getOrderCount() {
        // 订单统计
        Long undeliveredCount = tradeOrderStatisticsService.getCountByStatusAndDeliveryType(
                SalesOrderStatusEnum.UNDELIVERED.getStatus(), SalesDeliveryTypeEnum.EXPRESS.getType());
        // TODO @疯狂：订单支付后，如果是门店自提的，需要 update 成 DELIVERED；；目前还没搞~~突然反应过来
        Long pickUpCount = tradeOrderStatisticsService.getCountByStatusAndDeliveryType(
                SalesOrderStatusEnum.DELIVERED.getStatus(), SalesDeliveryTypeEnum.PICK_UP.getType());
        // 售后统计
        Long afterSaleApplyCount = afterSaleStatisticsService.getCountByStatus(SalesAfterSaleStatusEnum.APPLY);
        Long auditingWithdrawCount = brokerageStatisticsService.getWithdrawCountByStatus(SalesBrokerageWithdrawStatusEnum.AUDITING);
        // 拼接返回
        return success(SalesStatisticsConvert.INSTANCE.convert(undeliveredCount, pickUpCount, afterSaleApplyCount, auditingWithdrawCount));
    }

    @GetMapping("/order-comparison")
    @Operation(summary = "获得交易订单数量")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:query')")
    public CommonResult<SalesStatisticsDataComparisonRespVO<SalesTradeOrderSummaryRespVO>> getOrderComparison() {
        return success(tradeOrderStatisticsService.getOrderComparison());
    }

    @GetMapping("/order-count-trend")
    @Operation(summary = "获得订单量趋势统计")
    @PreAuthorize("@ss.hasPermission('sales:statistics_trade:query')")
    public CommonResult<List<SalesStatisticsDataComparisonRespVO<SalesOrderTrendRespVO>>> getOrderCountTrendComparison(@Valid SalesOrderTrendReqVO reqVO) {
        // TODO @疯狂：要注意 date 的排序；
        return success(tradeOrderStatisticsService.getOrderCountTrendComparison(reqVO));
    }

}
