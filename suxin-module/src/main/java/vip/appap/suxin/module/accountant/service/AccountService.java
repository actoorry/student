package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.admin.vo.AccountPageReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountDO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountTransactionDO;
import vip.appap.suxin.module.accountant.enums.AccountBizTypeEnum;

/**
 * 钱包 Service 接口
 *
 * @author jason
 */
public interface AccountService {

    /**
     * 获取钱包信息
     * <p>
     * 如果不存在，则创建钱包。由于用户注册时候不会创建钱包
     * 默认使用充值余额类型
     *
     * @param partnerId 客商编号
     */
    AccountDO getOrCreateAccount(Long partnerId);

    /**
     * 获取钱包信息（指定余额类型）
     * <p>
     * 如果不存在，则创建钱包。由于用户注册时候不会创建钱包
     *
     * @param partnerId   客商编号
     * @param balanceType 余额类型
     */
    AccountDO getOrCreateAccount(Long partnerId, String balanceType);

    /**
     * 获取钱包信息
     *
     * @param accountId 钱包 id
     */
    AccountDO getAccount(Long accountId);

    /**
     * 获得会员钱包分页
     *
     * @param pageReqVO 分页查询
     * @return 会员钱包分页
     */
    PageResult<AccountDO> getAccountPage(AccountPageReqVO pageReqVO);

    /**
     * 钱包订单支付
     *
     * @param accountId   钱包编号
     * @param outTradeNo 外部订单号
     * @param price      金额
     */
    AccountTransactionDO orderPay(Long accountId, String outTradeNo, Integer price);

    /**
     * 钱包订单支付退款
     *
     * @param outRefundNo 外部退款号
     * @param refundPrice 退款金额
     * @param reason      退款原因
     */
    AccountTransactionDO orderRefund(String outRefundNo, Integer refundPrice, String reason);

    /**
     * 扣减钱包余额
     *
     * @param accountId 钱包编号
     * @param bizId    业务关联编号
     * @param bizType  业务关联分类
     * @param price    扣减金额
     * @return 钱包流水
     */
    AccountTransactionDO reduceAccountBalance(Long accountId, Long bizId,
                                               AccountBizTypeEnum bizType, Integer price);

    /**
     * 增加钱包余额
     *
     * @param accountId 钱包编号
     * @param bizId    业务关联编号
     * @param bizType  业务关联分类
     * @param price    增加金额
     * @return 钱包流水
     */
    AccountTransactionDO addAccountBalance(Long accountId, String bizId,
                                            AccountBizTypeEnum bizType, Integer price);

    /**
     * 冻结钱包部分余额
     *
     * @param id    钱包编号
     * @param price 冻结金额
     */
    void freezePrice(Long id, Integer price);

    /**
     * 解冻钱包余额
     *
     * @param id    钱包编号
     * @param price 解冻金额
     */
    void unfreezePrice(Long id, Integer price);

}

