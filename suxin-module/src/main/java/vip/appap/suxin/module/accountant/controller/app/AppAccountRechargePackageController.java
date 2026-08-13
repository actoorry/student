package vip.appap.suxin.module.accountant.controller.app;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountPackageRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import vip.appap.suxin.module.accountant.service.AccountRechargePackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 钱包充值套餐")
@RestController
@RequestMapping("/accountant/account-recharge-package")
@Validated
@Slf4j
public class AppAccountRechargePackageController {

    @Resource
    private AccountRechargePackageService accountRechargePackageService;

    @GetMapping("/list")
    @Operation(summary = "获得钱包充值套餐列表")
    public CommonResult<List<AppAccountPackageRespVO>> getAccountRechargePackageList() {
        List<AccountRechargePackageDO> list = accountRechargePackageService.getAccountRechargePackageList(
                CommonStatusEnum.ENABLE.getStatus());
        list.sort(Comparator.comparingInt(AccountRechargePackageDO::getPayPrice));
        return success(BeanUtils.toBean(list, AppAccountPackageRespVO.class));
    }

}

