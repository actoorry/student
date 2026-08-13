package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;

import java.util.Collection;
import java.util.List;

/**
 * 电子面单订单记录 Service 接口
 *
 * @author 书心软件
 */
public interface SalesElectronicWaybillService {

    /**
     * 获得订单最新的电子面单记录（含失败、有效、作废）
     *
     * @param orderId 订单编号
     * @return 记录；可能为 null
     */
    SalesElectronicWaybillDO getLatestByOrderId(Long orderId);

    /** 获得订单最早的结果待确认记录，用于计算官方 48 小时幂等窗口。 */
    SalesElectronicWaybillDO getOldestUnknownByOrderId(Long orderId);

    /**
     * 获得订单的有效电子面单记录
     *
     * @param orderId 订单编号
     * @return 记录；可能为 null
     */
    SalesElectronicWaybillDO getValidByOrderId(Long orderId);

    /**
     * 批量获得订单的有效电子面单记录
     *
     * @param orderIds 订单编号集合
     * @return 记录列表
     */
    List<SalesElectronicWaybillDO> getValidListByOrderIds(Collection<Long> orderIds);

    /**
     * 插入电子面单记录（下单成功/失败均落一条记录）
     *
     * @param waybill 记录
     */
    void insertWaybill(SalesElectronicWaybillDO waybill);

    /**
     * 将有效电子面单标记为已作废（作废成功后调用）
     *
     * @param id           记录编号
     * @param cancelUserId 操作人
     * @param reason       作废原因
     */
    void markCanceled(Long id, Long cancelUserId, String reason);

    /**
     * 更新面单短链（复打恢复可打印内容时使用）
     *
     * @param id       记录编号
     * @param labelUrl 面单短链
     */
    void updateLabelUrl(Long id, String labelUrl);

}
