package vip.appap.suxin.module.accountant.service;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.accountant.controller.app.vo.AppAccountRechargeCreateReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.AccountRechargeDO;

/**
 * 钱包充值 Service 接口
 *
 * @author jason
 */
public interface AccountRechargeService {

    /**
     * 创建钱包充值记录（发起充值）
     *
     * @param userId      用户编号
     * @param createReqVO 钱包充值请求 VO
     * @param userIp  用户Ip
     * @return 钱包充值记录
     */
    AccountRechargeDO createAccountRecharge(Long userId, String userIp,
                                             AppAccountRechargeCreateReqVO createReqVO);

    /**
     * 获得钱包充值记录分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页请求
     * @param payStatus 是否支付
     * @return 钱包充值记录分页
     */
    PageResult<AccountRechargeDO> getAccountRechargePackagePage(Long userId,
                                                                 PageParam pageReqVO, Boolean payStatus);

    /**
     * 更新钱包充值成功
     *
     * @param id         钱包充值记录编号
     * @param payOrderId 支付订单编号
     */
    void updateAccountRechargePaid(Long id, Long payOrderId);

    /**
     * 发起钱包充值退款
     *
     * @param id     钱包充值编号
     * @param userIp 用户 ip 地址
     */
    void refundAccountRecharge(Long id, String userIp);

    /**
     * 更新钱包充值记录为已退款
     *
     * @param id          钱包充值记录编号
     * @param refundId    钱包充值退款编号（格式：{id}-refund）
     * @param payRefundId 退款单id
     */
    void updateAccountRechargeRefunded(Long id, String refundId, Long payRefundId);

}

