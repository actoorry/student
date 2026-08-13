package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPayStatisticsSummaryRespVO;
import vip.appap.suxin.module.sales.convert.SalesPayStatisticsConvert;
import vip.appap.suxin.module.sales.service.SalesPayStatisticsWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 支付统计")
@RestController
@RequestMapping("/sales/statistics/pay")
@Validated
@Slf4j
public class SalesPayStatisticsController {

    @Resource
    private SalesPayStatisticsWalletService payWalletStatisticsService;

    @GetMapping("/summary")
    @Operation(summary = "获取充值金额")
    public CommonResult<SalesPayStatisticsSummaryRespVO> getWalletRechargePrice() {
        Integer rechargePrice = payWalletStatisticsService.getRechargePriceSummary();
        return success(SalesPayStatisticsConvert.INSTANCE.convert(rechargePrice));
    }

}
