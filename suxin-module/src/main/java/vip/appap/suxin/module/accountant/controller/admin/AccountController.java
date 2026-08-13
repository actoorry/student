package vip.appap.suxin.module.accountant.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountPageReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRespVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountUpdateBalanceReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountUserReqVO;
import vip.appap.suxin.module.accountant.convert.AccountConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户钱包")
@RestController
@RequestMapping("/accountant/account")
@Validated
@Slf4j
public class AccountController {

    @Resource
    private AccountService AccountService;

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('accountant:account:query')")
    @Operation(summary = "获得用户钱包明细")
    public CommonResult<AccountRespVO> getAccount(AccountUserReqVO reqVO) {
        AccountDO account = AccountService.getOrCreateAccount(reqVO.getPartnerId());
        return success(AccountConvert.INSTANCE.convert02(account));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员钱包分页")
    @PreAuthorize("@ss.hasPermission('accountant:account:query')")
    public CommonResult<PageResult<AccountRespVO>> getAccountPage(@Valid AccountPageReqVO pageVO) {
        PageResult<AccountDO> pageResult = AccountService.getAccountPage(pageVO);
        return success(AccountConvert.INSTANCE.convertPage(pageResult));
    }

    @PutMapping("/update-balance")
    @Operation(summary = "更新会员用户余额")
    @PreAuthorize("@ss.hasPermission('accountant:account:update-balance')")
    public CommonResult<Boolean> updateWalletBalance(@Valid @RequestBody AccountUpdateBalanceReqVO updateReqVO) {
        // 获得用户钱包
        AccountDO account = AccountService.getOrCreateAccount(updateReqVO.getPartnerId());

        // 更新钱包余额
        AccountService.addAccountBalance(account.getId(), String.valueOf(updateReqVO.getPartnerId()),
                AccountBizTypeEnum.UPDATE_BALANCE, updateReqVO.getBalance());
        return success(true);
    }

}

