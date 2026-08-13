package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesConfigSaveReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesConfigDO;

import jakarta.validation.Valid;

/**
 * 交易中心配置 Service 接口
 *
 * @author owen
 */
public interface SalesConfigService {

    /**
     * 更新交易中心配置
     *
     * @param updateReqVO 更新信息
     */
    void saveTradeConfig(@Valid SalesConfigSaveReqVO updateReqVO);

    /**
     * 获得交易中心配置
     *
     * @return 交易中心配置
     */
    SalesConfigDO getTradeConfig();

}
