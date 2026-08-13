package vip.appap.suxin.module.sales.service;


import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleLogDO;
import vip.appap.suxin.module.sales.service.bo.SalesAfterSaleLogCreateReqBO;

import java.util.List;

/**
 * 交易售后日志 Service 接口
 *
 * @author 陈賝
 * @since 2023/6/12 14:18
 */
public interface SalesAfterSaleLogService {

    /**
     * 创建售后日志
     *
     * @param createReqBO 日志记录
     * @author 陈賝
     * @since 2023/6/12 14:18
     */
    void createAfterSaleLog(SalesAfterSaleLogCreateReqBO createReqBO);

    /**
     * 获取售后日志
     *
     * @param afterSaleId 售后编号
     * @return 售后日志
     */
    List<SalesAfterSaleLogDO> getAfterSaleLogList(Long afterSaleId);

}
