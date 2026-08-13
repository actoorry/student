package vip.appap.suxin.module.accountant.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionSummaryRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountTransactionRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.service.AccountTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 钱包余额明细")
@RestController
@RequestMapping("/accountant/account-transaction")
@Validated
@Slf4j
public class AppAccountTransactionController {

    @Resource
    private AccountTransactionService AccountTransactionService;

    @GetMapping("/page")
    @Operation(summary = "获得钱包流水分页")
    public CommonResult<PageResult<AppAccountTransactionRespVO>> getAccountTransactionPage(
            @Valid AppAccountTransactionPageReqVO pageReqVO) {
        PageResult<AccountTransactionDO> pageResult = AccountTransactionService.getAccountTransactionPage(
                getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AppAccountTransactionRespVO.class));
    }

    @GetMapping("/get-summary")
    @Operation(summary = "获得钱包流水统计")
    @Parameter(name = "times", description = "时间段", required = true)
    public CommonResult<AppAccountTransactionSummaryRespVO> getaccountTransactionSummary(
            @RequestParam("createTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime[] createTime) {
        AppAccountTransactionSummaryRespVO summary = AccountTransactionService.getaccountTransactionSummary(
                getLoginUserId(), createTime);
        return success(summary);
    }

}

