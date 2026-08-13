package vip.appap.suxin.module.accountant.controller.app;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayOrderRespVO;
import vip.appap.suxin.module.accountant.controller.admin.vo.PayOrderSubmitRespVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppPayOrderSubmitReqVO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppPayOrderSubmitRespVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.account.AccountPayClient;
import vip.appap.suxin.module.accountant.service.PayOrderService;
import vip.appap.suxin.module.accountant.service.AccountService;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.servlet.ServletUtils.getClientIP;
import static vip.appap.suxin.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 支付订单")
@RestController
@RequestMapping("/accountant/order")
@Validated
@Slf4j
public class AppPayOrderController {

    @Resource
    private PayOrderService payOrderService;
    @Resource
    private AccountService accountService;

    @GetMapping("/get")
    @Operation(summary = "获得支付订单")
    @Parameters({
            @Parameter(name = "id", description = "编号", example = "1024"),
            @Parameter(name = "no", description = "支付订单号", example = "Pxxx"),
            @Parameter(name = "sync", description = "是否同步", example = "true")
    })
    public CommonResult<PayOrderRespVO> getOrder(@RequestParam(value = "id", required = false) Long id,
                                                 @RequestParam(value = "no", required = false) String no,
                                                 @RequestParam(value = "sync", required = false) Boolean sync) {
        PayOrderDO order = null;
        if (CharSequenceUtil.isNotEmpty(no)) {
            order = payOrderService.getOrder(no);
        }
        if (ObjUtil.isNull(order) && ObjUtil.isNotNull(id)) {
            order = payOrderService.getOrder(id);
        }
        if (order == null) {
            return success(null);
        }
        // 重要：校验订单是否是当前用户，避免越权
        if (order.getUserId() != null // 特殊：早期订单未存储 userId，所以忽略
                && ObjUtil.notEqual(order.getUserId(), getLoginUserId())) {
            return success(null);
        }

        // sync 仅在等待支付
        if (Boolean.TRUE.equals(sync) && PayOrderStatusEnum.isWaiting(order.getStatus())) {
            payOrderService.syncOrderQuietly(order.getId());
            // 重新查询，因为同步后，可能会有变化
            order = payOrderService.getOrder(order.getId());
        }
        return success(BeanUtils.toBean(order, PayOrderRespVO.class));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交支付订单")
    public CommonResult<AppPayOrderSubmitRespVO> submitPayOrder(@RequestBody AppPayOrderSubmitReqVO reqVO) {
        // 1. 钱包支付事，需要额外传 user_id 和 user_type
        if (Objects.equals(reqVO.getChannelCode(), PayChannelEnum.ACCOUNT.getCode())) {
            if (reqVO.getChannelExtras() == null) {
                reqVO.setChannelExtras(Maps.newHashMapWithExpectedSize(1));
            }
            AccountDO account = accountService.getOrCreateAccount(getLoginUserId());
            reqVO.getChannelExtras().put(AccountPayClient.ACCOUNT_ID_KEY, String.valueOf(account.getId()));
        }

        // 2. 提交支付
        PayOrderSubmitRespVO respVO = payOrderService.submitOrder(reqVO, getClientIP());
        return success(BeanUtils.toBean(respVO, AppPayOrderSubmitRespVO.class));
    }

}

