package vip.appap.suxin.module.accountant.service;

import cn.hutool.core.lang.Assert;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.date.DateUtils;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountPageReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderExtensionDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayRefundDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.dal.mysql.AccountMapper;
import vip.appap.suxin.module.accountant.dal.redis.account.AccountLockRedisDAO;
import vip.appap.suxin.module.accountant.enums.AccountBalanceTypeEnum;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.service.PayOrderService;
import vip.appap.suxin.module.accountant.service.PayRefundService;
import vip.appap.suxin.module.accountant.service.bo.AccountTransactionCreateReqBO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.*;
import static vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum.PAYMENT;
import static vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum.PAYMENT_REFUND;

/**
 * 账户 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

    /**
     * 通知超时时间，单位：毫秒
     */
    public static final long UPDATE_TIMEOUT_MILLIS = 120 * DateUtils.SECOND_MILLIS;

    @Resource
    private AccountMapper accountMapper;
    @Resource
    private AccountLockRedisDAO lockRedisDAO;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private AccountTransactionService accountTransactionService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PayOrderService orderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private PayRefundService refundService;

    @Override
    @SneakyThrows
    public AccountDO getOrCreateAccount(Long partnerId) {
        return getOrCreateAccount(partnerId, AccountBalanceTypeEnum.RECHARGE.getType());
    }

    @Override
    @SneakyThrows
    public AccountDO getOrCreateAccount(Long partnerId, String balanceType) {
        AccountDO account = accountMapper.selectByPartnerIdAndBalanceType(partnerId, balanceType);
        if (account == null) {
            // 使用双重检查锁，保证钱包创建并发问题
            // https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1475/files
            account = lockRedisDAO.lock(partnerId, UPDATE_TIMEOUT_MILLIS, () -> {
                AccountDO newAccount = accountMapper.selectByPartnerIdAndBalanceType(partnerId, balanceType);
                if (newAccount == null) {
                    newAccount = new AccountDO().setPartnerId(partnerId)
                            .setBalanceType(balanceType)
                            .setBalance(0).setFreezePrice(0).setTotalExpense(0).setTotalRecharge(0);
                    newAccount.setCreateTime(LocalDateTime.now());
                    accountMapper.insert(newAccount);
                }
                return newAccount;
            });
        }
        return account;
    }

    @Override
    public AccountDO getAccount(Long accountId) {
        return accountMapper.selectById(accountId);
    }

    @Override
    public PageResult<AccountDO> getAccountPage(AccountPageReqVO pageReqVO) {
        return accountMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountTransactionDO orderPay(Long accountId, String outTradeNo, Integer price) {
        // 1. 判断支付交易拓展单是否存
        PayOrderExtensionDO orderExtension = orderService.getOrderExtensionByNo(outTradeNo);
        if (orderExtension == null) {
            throw exception(PAY_ORDER_EXTENSION_NOT_FOUND);
        }
        AccountDO account = accountMapper.selectById(accountId);
        // 2. 扣减余额
        return reduceAccountBalance(account.getId(), orderExtension.getOrderId(), PAYMENT, price);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountTransactionDO orderRefund(String outRefundNo, Integer refundPrice, String reason) {
        // 1.1 判断退款单是否存在
        PayRefundDO payRefund = refundService.getRefundByNo(outRefundNo);
        if (payRefund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        // 1.2 校验是否可以退款
        Long accountId = validateAccountCanRefund(payRefund.getId(), payRefund.getChannelOrderNo());
        AccountDO account = accountMapper.selectById(accountId);
        Assert.notNull(account, "钱包 {} 不存在", accountId);

        // 2. 增加余额
        return addAccountBalance(accountId, String.valueOf(payRefund.getId()), PAYMENT_REFUND, refundPrice);
    }

    /**
     * 校验是否能退款
     *
     * @param refundId 支付退款单 id
     * @param accountPayNo 钱包支付 no
     */
    private Long validateAccountCanRefund(Long refundId, String accountPayNo) {
        // 1. 校验钱包支付交易存在
        AccountTransactionDO accountTransaction = accountTransactionService.getAccountTransactionByNo(accountPayNo);
        if (accountTransaction == null) {
            throw exception(WALLET_TRANSACTION_NOT_FOUND);
        }
        // 2. 校验退款是否存在
        AccountTransactionDO refundTransaction = accountTransactionService.getAccountTransaction(
                String.valueOf(refundId), PAYMENT_REFUND);
        if (refundTransaction != null) {
            throw exception(WALLET_REFUND_EXIST);
        }
        return accountTransaction.getAccountId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public AccountTransactionDO reduceAccountBalance(Long accountId, Long bizId,
                                                      AccountBizTypeEnum bizType, Integer price) {
        // 1. 获取钱包
        AccountDO account = getAccount(accountId);
        if (account == null) {
            log.error("[reduceAccountBalance][用户钱包({})不存在]", accountId);
            throw exception(WALLET_NOT_FOUND);
        }

        // 2. 加锁，更新钱包余额（目的：避免钱包流水的并发更新时，余额变化不连贯）
        return lockRedisDAO.lock(accountId, UPDATE_TIMEOUT_MILLIS, () -> {
            // 2. 扣除余额
            int updateCounts;
            switch (bizType) {
                case PAYMENT: {
                    updateCounts = accountMapper.updateWhenConsumption(account.getId(), price);
                    break;
                }
                case RECHARGE_REFUND: {
                    updateCounts = accountMapper.updateWhenRechargeRefund(account.getId(), price);
                    break;
                }
                default: {
                    // TODO 其它类型待实现
                    throw new UnsupportedOperationException("待实现");
                }
            }
            if (updateCounts == 0) {
                throw exception(WALLET_BALANCE_NOT_ENOUGH);
            }

            // 3. 生成钱包流水
            // 情况一：充值退款：balance 在冻结时已扣，updateWhenRechargeRefund 只扣 freeze_price，所以 afterBalance 不变。https://t.zsxq.com/OJk9m
            // 情况二：消费支付：updateWhenConsumption 从 balance 扣，所以 afterBalance = balance - price
            Integer afterBalance = bizType == AccountBizTypeEnum.RECHARGE_REFUND
                    ? account.getBalance()
                    : account.getBalance() - price;
            AccountTransactionCreateReqBO bo = new AccountTransactionCreateReqBO().setAccountId(account.getId())
                    .setPrice(-price).setBalance(afterBalance).setBizId(String.valueOf(bizId))
                    .setBizType(bizType.getType()).setTitle(bizType.getDescription());
            return accountTransactionService.createAccountTransaction(bo);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public AccountTransactionDO addAccountBalance(Long accountId, String bizId,
                                                   AccountBizTypeEnum bizType, Integer price) {
        // 1. 获取钱包
        AccountDO account = getAccount(accountId);
        if (account == null) {
            log.error("[addAccountBalance][用户钱包({})不存在]", accountId);
            throw exception(WALLET_NOT_FOUND);
        }

        // 2. 加锁，更新钱包余额（目的：避免钱包流水的并发更新时，余额变化不连贯）
        return lockRedisDAO.lock(accountId, UPDATE_TIMEOUT_MILLIS, () -> {
            // 3. 更新钱包金额
            switch (bizType) {
                case PAYMENT_REFUND: { // 退款更新
                    accountMapper.updateWhenConsumptionRefund(account.getId(), price);
                    break;
                }
                case RECHARGE: { // 充值更新
                    accountMapper.updateWhenRecharge(account.getId(), price);
                    break;
                }
                case UPDATE_BALANCE: // 更新余额
                case TRANSFER: // 分佣提现
                    accountMapper.updateWhenAdd(account.getId(), price);
                    break;
                default: {
                    throw new UnsupportedOperationException("待实现：" + bizType);
                }
            }

            // 4. 生成钱包流水
            AccountTransactionCreateReqBO transactionCreateReqBO = new AccountTransactionCreateReqBO()
                    .setAccountId(account.getId()).setPrice(price).setBalance(account.getBalance() + price)
                    .setBizId(bizId).setBizType(bizType.getType()).setTitle(bizType.getDescription());
            return accountTransactionService.createAccountTransaction(transactionCreateReqBO);
        });
    }

    @Override
    public void freezePrice(Long id, Integer price) {
        int updateCounts = accountMapper.freezePrice(id, price);
        if (updateCounts == 0) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }
    }

    @Override
    public void unfreezePrice(Long id, Integer price) {
        int updateCounts = accountMapper.unFreezePrice(id, price);
        if (updateCounts == 0) {
            throw exception(WALLET_FREEZE_PRICE_NOT_ENOUGH);
        }
    }

}

