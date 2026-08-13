package vip.appap.suxin.module.accountant.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.api.dto.PayOrderCreateReqDTO;
import vip.appap.suxin.module.accountant.api.PayRefundApi;
import vip.appap.suxin.module.accountant.api.dto.PayRefundCreateReqDTO;
import vip.appap.suxin.module.accountant.api.dto.PayRefundRespDTO;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeCreateReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargeDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargePackageDO;
import vip.appap.suxin.module.accountant.dal.mysql.AccountRechargeMapper;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayRefundStatusEnum;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.framework.pay.config.PayProperties;
import vip.appap.suxin.module.accountant.service.PayOrderService;
import vip.appap.suxin.module.system.api.SocialClientApi;
import vip.appap.suxin.module.system.api.dto.SocialWxaOrderUploadShippingInfoReqDTO;
import vip.appap.suxin.module.system.api.dto.SocialWxaSubscribeMessageSendReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.addTime;
import static vip.appap.suxin.framework.common.util.json.JsonUtils.toJsonString;
import static vip.appap.suxin.framework.common.util.number.MoneyUtils.fenToYuanStr;
import static vip.appap.suxin.module.accountant.convert.AccountRechargeConvert.INSTANCE;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.*;
import static vip.appap.suxin.module.accountant.enums.MessageTemplateConstants.ACCOUNT_RECHARGER_PAID;
import static vip.appap.suxin.module.accountant.enums.PayRefundStatusEnum.*;

/**
 * 钱包充值 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class AccountRechargeServiceImpl implements AccountRechargeService {

    private static final String WALLET_RECHARGE_ORDER_SUBJECT = "Account Recharge";

    @Resource
    private AccountRechargeMapper accountRechargeMapper;
    @Resource
    private AccountService accountService;
    @Resource
    private PayOrderService payOrderService;
    @Resource
    private AccountRechargePackageService accountRechargePackageService;

    @Resource
    public SocialClientApi socialClientApi;
    @Resource
    private PayRefundApi payRefundApi;

    @Resource
    private PayProperties payProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountRechargeDO createAccountRecharge(Long userId, String userIp,
                                                    AppAccountRechargeCreateReqVO reqVO) {
        // 1.1 获取支付金额和赠送金额
        int payPrice;
        int bonusPrice = 0;
        if (Objects.nonNull(reqVO.getPackageId())) {
            AccountRechargePackageDO rechargePackage = accountRechargePackageService.validAccountRechargePackage(reqVO.getPackageId());
            payPrice = rechargePackage.getPayPrice();
            bonusPrice = rechargePackage.getBonusPrice();
        } else {
            payPrice = reqVO.getPayPrice();
        }
        // 1.2 创建钱包充值记录
        AccountDO account = accountService.getOrCreateAccount(userId);
        AccountRechargeDO recharge = INSTANCE.convert(account.getId(), payPrice, bonusPrice, reqVO.getPackageId());
        accountRechargeMapper.insert(recharge);

        // 2.1 创建支付订单
        Long payOrderId = payOrderService.createOrder(new PayOrderCreateReqDTO()
                .setAppKey(payProperties.getAccountPayAppKey()).setUserIp(userIp)
                .setUserId(userId).setUserType(UserTypeEnum.MEMBER.getValue())
                .setMerchantOrderId(recharge.getId().toString())
                .setSubject(WALLET_RECHARGE_ORDER_SUBJECT).setBody("")
                .setPrice(recharge.getPayPrice())
                .setExpireTime(addTime(Duration.ofHours(2L))));
        // 2.2 关联支付订单
        accountRechargeMapper.updateById(new AccountRechargeDO().setId(recharge.getId()).setPayOrderId(payOrderId));
        recharge.setPayOrderId(payOrderId);
        return recharge;
    }

    @Override
    public PageResult<AccountRechargeDO> getAccountRechargePackagePage(Long userId, PageParam pageReqVO, Boolean payStatus) {
        AccountDO account = accountService.getOrCreateAccount(userId);
        return accountRechargeMapper.selectPage(pageReqVO, account.getId(), payStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountRechargePaid(Long id, Long payOrderId) {
        // 1.1 校验充值记录
        AccountRechargeDO recharge = accountRechargeMapper.selectById(id);
        if (recharge == null) {
            log.error("[updateAccountRechargePaid][recharge({}) payOrder({}) 充值记录不存在]", id, payOrderId);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验是否已支付
        if (recharge.getPayStatus()) {
            if (ObjectUtil.equals(recharge.getPayOrderId(), payOrderId)) {
                log.warn("[updateAccountRechargePaid][recharge({}) already paid with same payOrderId({})]", recharge, payOrderId);
                return;
            }
            log.error("[updateAccountRechargePaid][recharge({}) already paid but payOrderId differs({})]", recharge, payOrderId);
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }

        // 2. 校验支付订单已支付
        PayOrderDO payOrderDO = validatePayOrderPaid(recharge, payOrderId);

        // 3. 更新充值记录为已支付
        int updateCount = accountRechargeMapper.updateByIdAndPaid(id, false,
                new AccountRechargeDO().setId(id).setPayStatus(true).setPayTime(LocalDateTime.now())
                        .setPayChannelCode(payOrderDO.getChannelCode()));
        if (updateCount == 0) {
            throw exception(WALLET_RECHARGE_UPDATE_PAID_STATUS_NOT_UNPAID);
        }

        // 4. 增加钱包余额
        accountService.addAccountBalance(recharge.getAccountId(), String.valueOf(id),
                AccountBizTypeEnum.RECHARGE, recharge.getTotalPrice());

        // 5. 发送充值成功微信订阅消息
        getSelf().sendAccountRechargePaidMessage(payOrderId, recharge);
    }

    @Async
    public void sendAccountRechargePaidMessage(Long payOrderId, AccountRechargeDO accountRecharge) {
        // 1. 获取钱包信息
        AccountDO account = accountService.getAccount(accountRecharge.getAccountId());
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO()
                .setUserId(account.getPartnerId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setTemplateTitle(ACCOUNT_RECHARGER_PAID)
                .setPage("pages/user/account/money")
                .addMessage("character_string1", String.valueOf(payOrderId))
                .addMessage("amount2", fenToYuanStr(accountRecharge.getTotalPrice()))
                .addMessage("time3", LocalDateTimeUtil.formatNormal(accountRecharge.getCreateTime()))
        );

        // 2. 微信小程序虚拟商品发货
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (ObjUtil.notEqual(payOrder.getChannelCode(), PayChannelEnum.WX_LITE.getCode())) {
            return;
        }
        SocialWxaOrderUploadShippingInfoReqDTO reqDTO = new SocialWxaOrderUploadShippingInfoReqDTO()
                .setTransactionId(payOrder.getChannelOrderNo())
                .setOpenid(payOrder.getChannelUserId())
                .setItemDesc(payOrder.getSubject())
                .setLogisticsType(SocialWxaOrderUploadShippingInfoReqDTO.LOGISTICS_TYPE_VIRTUAL);
        try {
            socialClientApi.uploadWxaOrderShippingInfo(UserTypeEnum.MEMBER.getValue(), reqDTO);
        } catch (Exception ex) {
            log.error("[sendAccountRechargePaidMessage][充值({}) 微信发货上传失败]", payOrder, ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundAccountRecharge(Long id, String userIp) {
        // 1.1 校验充值记录
        AccountRechargeDO accountRecharge = accountRechargeMapper.selectById(id);
        if (accountRecharge == null) {
            log.error("[refundAccountRecharge][充值记录({}) 不存在]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验是否可以退款
        AccountDO account = validateAccountRechargeCanRefund(accountRecharge);

        // 2. 冻结钱包余额
        accountService.freezePrice(account.getId(), accountRecharge.getTotalPrice());

        // 3. 发起退款
        String accountRechargeId = String.valueOf(id);
        String refundId = accountRechargeId + "-refund";
        Long payRefundId = payRefundApi.createRefund(new PayRefundCreateReqDTO()
                .setAppKey(payProperties.getAccountPayAppKey()).setUserIp(userIp)
                .setUserId(account.getPartnerId()).setUserType(UserTypeEnum.MEMBER.getValue())
                .setMerchantOrderId(accountRechargeId)
                .setMerchantRefundId(refundId)
                .setReason("refund").setPrice(accountRecharge.getPayPrice()));

        // 4. 更新充值记录的退款信息
        accountRechargeMapper.updateById(new AccountRechargeDO().setPayRefundId(payRefundId)
                .setRefundStatus(WAITING.getStatus()).setId(accountRecharge.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccountRechargeRefunded(Long id, String refundId, Long payRefundId) {
        // 1.1 校验充值记录
        AccountRechargeDO accountRecharge = accountRechargeMapper.selectById(id);
        if (accountRecharge == null) {
            log.error("[updateAccountRechargePaid][充值记录({}) 不存在]", id);
            throw exception(WALLET_RECHARGE_NOT_FOUND);
        }
        // 1.2 校验退款单
        PayRefundRespDTO payRefund = validateAccountRechargeCanRefunded(accountRecharge, payRefundId);

        // 2. 根据退款结果更新
        AccountRechargeDO updateObj = new AccountRechargeDO().setId(id);
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            // 2.1 退款成功，扣减钱包余额
            accountService.reduceAccountBalance(accountRecharge.getAccountId(), id,
                    AccountBizTypeEnum.RECHARGE_REFUND, accountRecharge.getTotalPrice());

            updateObj.setRefundStatus(SUCCESS.getStatus()).setRefundTime(payRefund.getSuccessTime())
                    .setRefundTotalPrice(accountRecharge.getTotalPrice()).setRefundPayPrice(accountRecharge.getPayPrice())
                    .setRefundBonusPrice(accountRecharge.getBonusPrice());
        } else if (PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            // 2.2 退款失败，解冻钱包余额
            accountService.unfreezePrice(accountRecharge.getAccountId(), accountRecharge.getTotalPrice());

            updateObj.setRefundStatus(FAILURE.getStatus());
        }

        // 3. 更新充值记录退款状态
        accountRechargeMapper.updateByIdAndRefunded(id, WAITING.getStatus(), updateObj);
    }

    private PayRefundRespDTO validateAccountRechargeCanRefunded(AccountRechargeDO accountRecharge, Long payRefundId) {
        // 1. 校验退款单ID匹配
        if (notEqual(accountRecharge.getPayRefundId(), payRefundId)) {
            log.error("[validateAccountRechargeCanRefunded][充值({}) 退款单({}) 不匹配]", accountRecharge.getId(), payRefundId);
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }

        // 2.1 校验退款单存在
        PayRefundRespDTO payRefund = payRefundApi.getRefund(payRefundId);
        if (payRefund == null) {
            log.error("[validateAccountRechargeCanRefunded][recharge({}) refund({}) data mismatch]", accountRecharge.getId(), payRefundId);
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_NOT_FOUND);
        }
        // 2.2 校验退款金额匹配
        if (notEqual(payRefund.getRefundPrice(), accountRecharge.getPayPrice())) {
            log.error("[validateAccountRechargeCanRefunded][充值({}) 退款单({}) 金额不匹配]", accountRecharge.getId(), payRefundId);
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_PRICE_NOT_MATCH);
        }
        // 2.3 校验商户退款单号匹配
        if (notEqual(payRefund.getMerchantRefundId(), accountRecharge.getId() + "-refund")) {
            log.error("[validateAccountRechargeCanRefunded][充值({}) 退款单({}) 商户退款单号不匹配]", accountRecharge.getId(), payRefundId);
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUND_ORDER_ID_ERROR);
        }
        return payRefund;
    }

    private AccountDO validateAccountRechargeCanRefund(AccountRechargeDO accountRecharge) {
        // 校验是否已支付
        if (!accountRecharge.getPayStatus()) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_NOT_PAID);
        }
        // 校验是否已退款
        if (accountRecharge.getPayRefundId() != null) {
            throw exception(WALLET_RECHARGE_REFUND_FAIL_REFUNDED);
        }
        // 校验钱包余额是否足够
        AccountDO account = accountService.getAccount(accountRecharge.getAccountId());
        Assert.notNull(account, "User Account({}) not found", account.getId());
        if (account.getBalance() < accountRecharge.getTotalPrice()) {
            throw exception(WALLET_RECHARGE_REFUND_BALANCE_NOT_ENOUGH);
        }
        return account;
    }

    /**
     * 校验支付订单已支付
     *
     * @param recharge 充值记录
     * @param payOrderId 支付订单ID
     * @return 支付订单
     */
    private PayOrderDO validatePayOrderPaid(AccountRechargeDO recharge, Long payOrderId) {
        // 1. 校验支付订单存在
        PayOrderDO payOrder = payOrderService.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[updateAccountRechargeRefunded][payOrder({}) not found]", payOrderId);
            throw exception(PAY_ORDER_NOT_FOUND);
        }

        // 2.1 校验支付成功
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][充值({}) payOrder({}) 未支付成功]", recharge.getId(), payOrderId);
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.2 校验金额匹配
        if (notEqual(payOrder.getPrice(), recharge.getPayPrice())) {
            log.error("[validatePayOrderPaid][充值({}) payOrder({}) 支付金额不匹配]", recharge.getId(), payOrderId);
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_PRICE_NOT_MATCH);
        }
        // 2.3 校验商户订单号匹配
        if (notEqual(payOrder.getMerchantOrderId(), recharge.getId().toString())) {
            log.error("[validatePayOrderPaid][充值({}) payOrder({}) 商户订单号不匹配]", recharge.getId(), payOrderId);
            throw exception(WALLET_RECHARGE_UPDATE_PAID_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private AccountRechargeServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
