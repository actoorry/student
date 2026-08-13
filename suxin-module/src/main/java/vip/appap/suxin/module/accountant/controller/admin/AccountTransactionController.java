package vip.appap.suxin.module.accountant.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountTransactionPageReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountTransactionRespVO;
import vip.appap.suxin.module.accountant.convert.AccountTransactionConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.service.AccountTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 钱包余额明细")
@RestController
@RequestMapping("/accountant/account-transaction")
@Validated
@Slf4j
public class AccountTransactionController {

    @Resource
    private AccountTransactionService AccountTransactionService;

    @GetMapping("/page")
    @Operation(summary = "获得钱包流水分页")
    @PreAuthorize("@ss.hasPermission('accountant:account:query')")
    public CommonResult<PageResult<AccountTransactionRespVO>> getAccountTransactionPage(
            @Valid AccountTransactionPageReqVO pageReqVO) {
        PageResult<AccountTransactionDO> result = AccountTransactionService.getAccountTransactionPage(pageReqVO);
        return success(AccountTransactionConvert.INSTANCE.convertPage2(result));
    }

}

