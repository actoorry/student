package vip.appap.suxin.module.accountant.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeCreateReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeCreateRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeRespVO;
import vip.appap.suxin.module.accountant.convert.AccountConvert;
import vip.appap.suxin.module.accountant.convert.AccountRechargeConvert;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargeDO;
import vip.appap.suxin.module.accountant.service.PayOrderService;
import vip.appap.suxin.module.accountant.service.AccountRechargeService;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 钱包充值")
@RestController
@RequestMapping("/accountant/account-recharge")
@Validated
@Slf4j
public class AppAccountRechargeController {

    @Resource
    private AccountRechargeService accountRechargeService;
    @Resource
    private PayOrderService payOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建钱包充值记录（发起充值）")
    public CommonResult<AppAccountRechargeCreateRespVO> createAccountRecharge(
            @Valid @RequestBody  AppAccountRechargeCreateReqVO reqVO) {
        AccountRechargeDO accountRecharge = accountRechargeService.createAccountRecharge(
                getLoginUserId(), getClientIP(), reqVO);
        return success(AccountRechargeConvert.INSTANCE.convert(accountRecharge));
    }

    @GetMapping("/page")
    @Operation(summary = "获得钱包充值记录分页")
    public CommonResult<PageResult<AppAccountRechargeRespVO>> getWalletRechargePage(@Valid PageParam pageReqVO) {
        PageResult<AccountRechargeDO> pageResult = accountRechargeService.getAccountRechargePackagePage(
                getLoginUserId(), pageReqVO, true);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接数据
        List<PayOrderDO> payOrderList = payOrderService.getOrderList(
                convertList(pageResult.getList(), AccountRechargeDO::getPayOrderId));
        return success(AccountRechargeConvert.INSTANCE.convertPage(pageResult, payOrderList));
    }

}

