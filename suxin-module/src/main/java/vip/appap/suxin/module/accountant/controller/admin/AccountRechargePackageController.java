package vip.appap.suxin.module.accountant.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageCreateReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackagePageReqVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageRespVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountRechargePackageUpdateReqVO;
import vip.appap.suxin.module.accountant.convert.AccountRechargePackageConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import vip.appap.suxin.module.accountant.service.AccountRechargePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;


@Tag(name = "管理后台 - 钱包充值套餐")
@RestController
@RequestMapping("/accountant/account-recharge-package")
@Validated
public class AccountRechargePackageController {

    @Resource
    private AccountRechargePackageService accountRechargePackageService;

    @PostMapping("/create")
    @Operation(summary = "创建钱包充值套餐")
    @PreAuthorize("@ss.hasPermission('accountant:account-recharge-package:create')")
    public CommonResult<Long> createAccountRechargePackage(@Valid @RequestBody AccountRechargePackageCreateReqVO createReqVO) {
        return success(accountRechargePackageService.createAccountRechargePackage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新钱包充值套餐")
    @PreAuthorize("@ss.hasPermission('accountant:account-recharge-package:update')")
    public CommonResult<Boolean> updateAccountRechargePackage(@Valid @RequestBody AccountRechargePackageUpdateReqVO updateReqVO) {
        accountRechargePackageService.updateAccountRechargePackage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除钱包充值套餐")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('accountant:account-recharge-package:delete')")
    public CommonResult<Boolean> deleteAccountRechargePackage(@RequestParam("id") Long id) {
        accountRechargePackageService.deleteAccountRechargePackage(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得钱包充值套餐")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('accountant:account-recharge-package:query')")
    public CommonResult<AccountRechargePackageRespVO> getAccountRechargePackage(@RequestParam("id") Long id) {
        AccountRechargePackageDO accountRechargePackage = accountRechargePackageService.getAccountRechargePackage(id);
        return success(AccountRechargePackageConvert.INSTANCE.convert(accountRechargePackage));
    }

    @GetMapping("/page")
    @Operation(summary = "获得钱包充值套餐分页")
    @PreAuthorize("@ss.hasPermission('accountant:account-recharge-package:query')")
    public CommonResult<PageResult<AccountRechargePackageRespVO>> getAccountRechargePackagePage(@Valid AccountRechargePackagePageReqVO pageVO) {
        PageResult<AccountRechargePackageDO> pageResult = accountRechargePackageService.getAccountRechargePackagePage(pageVO);
        return success(AccountRechargePackageConvert.INSTANCE.convertPage(pageResult));
    }

}

