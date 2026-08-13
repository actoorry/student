package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.account;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayRefundStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayTransferStatusEnum;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.order.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.AbstractPayClient;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.NonePayClientConfig;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderExtensionDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayRefundDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayTransferDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;
import vip.appap.suxin.module.accountant.service.PayOrderService;
import vip.appap.suxin.module.accountant.service.PayRefundService;
import vip.appap.suxin.module.accountant.service.PayTransferService;
import vip.appap.suxin.module.accountant.service.AccountService;
import vip.appap.suxin.module.accountant.service.AccountTransactionService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.PAY_ORDER_EXTENSION_NOT_FOUND;
import static vip.appap.suxin.module.accountant.enums.ErrorCodeConstants.REFUND_NOT_FOUND;

/**
 * 钱包支付的 PayClient 实现类
 *
 * @author jason
 */
@Slf4j
public class AccountPayClient extends AbstractPayClient<NonePayClientConfig> {

    public static final String ACCOUNT_ID_KEY = "accountId";

    private AccountService wallService;
    private AccountTransactionService accountTransactionService;

    private PayOrderService orderService;
    private PayRefundService refundService;
    private PayTransferService transferService;

    public AccountPayClient(Long channelId, NonePayClientConfig config) {
        super(channelId, PayChannelEnum.ACCOUNT.getCode(), config);
    }

    @Override
    protected void doInit() {
        if (wallService == null) {
            wallService = SpringUtil.getBean(AccountService.class);
        }
        if (accountTransactionService == null) {
            accountTransactionService = SpringUtil.getBean(AccountTransactionService.class);
        }
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) {
        try {
            Long accountId = MapUtil.getLong(reqDTO.getChannelExtras(), ACCOUNT_ID_KEY);
            Assert.notNull(accountId, "钱包编号");
            AccountTransactionDO transaction = wallService.orderPay(accountId,
                    reqDTO.getOutTradeNo(), reqDTO.getPrice());
            return PayOrderRespDTO.successOf(transaction.getNo(), transaction.getCreator(),
                    transaction.getCreateTime(),
                    reqDTO.getOutTradeNo(), transaction);
        } catch (Throwable ex) {
            log.error("[doUnifiedOrder][reqDTO({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) ex;
                errorCode = serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayOrderRespDTO.closedOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutTradeNo(), "");
        }
    }

    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("钱包支付无支付回调");
    }

    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) {
        if (orderService == null) {
            orderService = SpringUtil.getBean(PayOrderService.class);
        }
        PayOrderExtensionDO orderExtension = orderService.getOrderExtensionByNo(outTradeNo);
        // 支付交易拓展单不存在， 返回关闭状态
        if (orderExtension == null) {
            return PayOrderRespDTO.closedOf(String.valueOf(PAY_ORDER_EXTENSION_NOT_FOUND.getCode()),
                    PAY_ORDER_EXTENSION_NOT_FOUND.getMsg(), outTradeNo, "");
        }
        // 关闭状态
        if (PayOrderStatusEnum.isClosed(orderExtension.getStatus())) {
            return PayOrderRespDTO.closedOf(orderExtension.getChannelErrorCode(),
                    orderExtension.getChannelErrorMsg(), outTradeNo, "");
        }
        // 成功状态
        if (PayOrderStatusEnum.isSuccess(orderExtension.getStatus())) {
            AccountTransactionDO accountTransaction = accountTransactionService.getAccountTransaction(
                    String.valueOf(orderExtension.getOrderId()), AccountBizTypeEnum.PAYMENT);
            Assert.notNull(accountTransaction, "支付单 {} 钱包流水不能为空", outTradeNo);
            return PayOrderRespDTO.successOf(accountTransaction.getNo(), accountTransaction.getCreator(),
                    accountTransaction.getCreateTime(), outTradeNo, accountTransaction);
        }
        // 其它状态为无效状态
        log.error("[doGetOrder] 支付单 {} 的状态不正确", outTradeNo);
        throw new IllegalStateException(String.format("支付单[%s] 状态不正确", outTradeNo));
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) {
        try {
            AccountTransactionDO AccountTransaction = wallService.orderRefund(reqDTO.getOutRefundNo(),
                    reqDTO.getRefundPrice(), reqDTO.getReason());
            return PayRefundRespDTO.successOf(AccountTransaction.getNo(), AccountTransaction.getCreateTime(),
                    reqDTO.getOutRefundNo(), AccountTransaction);
        } catch (Throwable ex) {
            log.error("[doUnifiedRefund][reqDOT({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) ex;
                errorCode =  serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayRefundRespDTO.failureOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutRefundNo(), "");
        }
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("钱包支付无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        if (refundService == null) {
            refundService = SpringUtil.getBean(PayRefundService.class);
        }
        PayRefundDO payRefund = refundService.getRefundByNo(outRefundNo);
        // 支付退款单不存在， 返回退款失败状态
        if (payRefund == null) {
            return PayRefundRespDTO.failureOf(String.valueOf(REFUND_NOT_FOUND), REFUND_NOT_FOUND.getMsg(),
                    outRefundNo, "");
        }
        // 退款失败
        if (PayRefundStatusEnum.isFailure(payRefund.getStatus())) {
            return PayRefundRespDTO.failureOf(payRefund.getChannelErrorCode(), payRefund.getChannelErrorMsg(),
                    outRefundNo, "");
        }
        // 退款成功
        if (PayRefundStatusEnum.isSuccess(payRefund.getStatus())) {
            AccountTransactionDO accountTransaction = accountTransactionService.getAccountTransaction(
                    String.valueOf(payRefund.getId()), AccountBizTypeEnum.PAYMENT_REFUND);
            Assert.notNull(accountTransaction, "支付退款单 {} 钱包流水不能为空", outRefundNo);
            return PayRefundRespDTO.successOf(accountTransaction.getNo(), accountTransaction.getCreateTime(),
                    outRefundNo, accountTransaction);
        }
        // 其它状态为无效状态
        log.error("[doGetRefund] 支付退款单 {} 的状态不正确", outRefundNo);
        throw new IllegalStateException(String.format("支付退款单[%s] 状态不正确", outRefundNo));
    }

    @Override
    @SuppressWarnings("PatternVariableCanBeUsed")
    public PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) {
        try {
            Long accountId = Long.parseLong(reqDTO.getUserAccount());
            AccountTransactionDO transaction = wallService.addAccountBalance(accountId, String.valueOf(reqDTO.getOutTransferNo()),
                    AccountBizTypeEnum.TRANSFER, reqDTO.getPrice());
            return PayTransferRespDTO.successOf(transaction.getNo(), transaction.getCreateTime(),
                    reqDTO.getOutTransferNo(), transaction);
        } catch (Throwable ex) {
            log.error("[doUnifiedTransfer][reqDTO({}) 异常]", reqDTO, ex);
            Integer errorCode = INTERNAL_SERVER_ERROR.getCode();
            String errorMsg = INTERNAL_SERVER_ERROR.getMsg();
            if (ex instanceof ServiceException) {
                ServiceException serviceException = (ServiceException) ex;
                errorCode = serviceException.getCode();
                errorMsg = serviceException.getMessage();
            }
            return PayTransferRespDTO.closedOf(String.valueOf(errorCode), errorMsg,
                    reqDTO.getOutTransferNo(), "");
        }
    }

    @Override
    protected PayTransferRespDTO doParseTransferNotify(Map<String, String> params, String body, Map<String, String> headers) {
        throw new UnsupportedOperationException("钱包支付无转账回调");
    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo) {
        if (transferService == null) {
            transferService = SpringUtil.getBean(PayTransferService.class);
        }
        // 获取转账单
        PayTransferDO transfer = transferService.getTransferByNo(outTradeNo);
        // 转账单不存在，返回关闭状态
        if (transfer == null) {
            return PayTransferRespDTO.closedOf(String.valueOf(PAY_ORDER_EXTENSION_NOT_FOUND.getCode()),
                    PAY_ORDER_EXTENSION_NOT_FOUND.getMsg(), outTradeNo, "");
        }
        // 关闭状态
        if (PayTransferStatusEnum.isClosed(transfer.getStatus())) {
            return PayTransferRespDTO.closedOf(transfer.getChannelErrorCode(),
                    transfer.getChannelErrorMsg(), outTradeNo, "");
        }
        // 成功状态
        if (PayTransferStatusEnum.isSuccess(transfer.getStatus())) {
            AccountTransactionDO accountTransaction = accountTransactionService.getAccountTransaction(
                    String.valueOf(transfer.getId()), AccountBizTypeEnum.TRANSFER);
            Assert.notNull(accountTransaction, "转账单 {} 钱包流水不能为空", outTradeNo);
            return PayTransferRespDTO.successOf(accountTransaction.getNo(), accountTransaction.getCreateTime(),
                    outTradeNo, accountTransaction);
        }
        // 处理中状态
        if (PayTransferStatusEnum.isProcessing(transfer.getStatus())) {
            return PayTransferRespDTO.processingOf(transfer.getChannelTransferNo(),
                    outTradeNo, transfer);
        }
        // 等待状态
        if (PayTransferStatusEnum.isWaiting(transfer.getStatus())) {
            return PayTransferRespDTO.waitingOf(transfer.getChannelTransferNo(),
                    outTradeNo, transfer);
        }
        // 其它状态为无效状态
        log.error("[doGetTransfer] 转账单 {} 的状态不正确", outTradeNo);
        throw new IllegalStateException(String.format("转账单[%s] 状态不正确", outTradeNo));
    }

}

