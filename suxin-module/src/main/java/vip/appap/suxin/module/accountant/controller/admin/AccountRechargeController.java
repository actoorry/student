package vip.appap.suxin.module.accountant.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.accountant.api.dto.PayOrderNotifyReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundNotifyReqDTO;
import vip.appap.suxin.module.accountant.service.AccountRechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;

@Tag(name = "管理后台 - 钱包充值")
@RestController
@RequestMapping("/accountant/account-recharge")
@Validated
@Slf4j
public class AccountRechargeController {

    @Resource
    private AccountRechargeService accountRechargeService;

    @PostMapping("/update-paid")
    @Operation(summary = "更新钱包充值为已充值") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录， 内部校验实现
    public CommonResult<Boolean> updateAccountRechargePaid(@Valid @RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        accountRechargeService.updateAccountRechargePaid(Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getPayOrderId());
        return success(true);
    }

    @PostMapping("/refund")
    @Operation(summary = "发起钱包充值退款")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<Boolean> refundAccountRecharge(@RequestParam("id") Long id) {
        accountRechargeService.refundAccountRecharge(id, getClientIP());
        return success(true);
    }

    @PostMapping("/update-refunded")
    @Operation(summary = "更新钱包充值为已退款") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录， 内部校验实现
    public CommonResult<Boolean> updateAccountRechargeRefunded(@RequestBody PayRefundNotifyReqDTO notifyReqDTO) {
        accountRechargeService.updateAccountRechargeRefunded(
                Long.valueOf(notifyReqDTO.getMerchantOrderId()),
                notifyReqDTO.getMerchantRefundId(),
                notifyReqDTO.getPayRefundId());
        return success(true);
    }

}

