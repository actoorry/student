package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesBrokerageWithdrawPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBrokerageWithdrawCreateReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageWithdrawDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageWithdrawStatusEnum;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageWithdrawSummaryRespBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 佣金提现 Service 接口
 *
 * @author 书心软件
 */
public interface SalesBrokerageWithdrawService {

    /**
     * 【管理员】审核佣金提现
     *
     * @param id          佣金编号
     * @param status      审核状态
     * @param auditReason 驳回原因
     * @param userIp 操作 IP
     */
    void auditBrokerageWithdraw(Long id, SalesBrokerageWithdrawStatusEnum status, String auditReason, String userIp);

    /**
     * 获得佣金提现
     *
     * @param id 编号
     * @return 佣金提现
     */
    SalesBrokerageWithdrawDO getBrokerageWithdraw(Long id);

    /**
     * 获得佣金提现分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金提现分页
     */
    PageResult<SalesBrokerageWithdrawDO> getBrokerageWithdrawPage(SalesBrokerageWithdrawPageReqVO pageReqVO);

    /**
     * 【会员】创建佣金提现
     *
     * @param userId      会员用户编号
     * @param createReqVO 创建信息
     * @return 佣金提现编号
     */
    Long createBrokerageWithdraw(Long userId, AppSalesBrokerageWithdrawCreateReqVO createReqVO);

    /**
     * 【API】更新佣金提现的转账结果
     *
     * 目前用于支付回调，标记提现转账结果
     *
     * @param id 提现编号
     * @param payTransferId 转账订单编号
     */
    void updateBrokerageWithdrawTransferred(Long id, Long payTransferId);

    /**
     * 按照 userId，汇总每个用户的提现
     *
     * @param userIds 用户编号
     * @param status  提现状态
     * @return 用户提现汇总 List
     */
    List<SalesBrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                        Collection<SalesBrokerageWithdrawStatusEnum> status);

    /**
     * 按照 userId，汇总每个用户的提现
     *
     * @param userIds 用户编号
     * @param status  提现状态
     * @return 用户提现汇总 Map
     */
    default Map<Long, SalesBrokerageWithdrawSummaryRespBO> getWithdrawSummaryMapByUserId(Set<Long> userIds,
                                                                                    Collection<SalesBrokerageWithdrawStatusEnum> status) {
        return convertMap(getWithdrawSummaryListByUserId(userIds, status), SalesBrokerageWithdrawSummaryRespBO::getUserId);
    }

}
