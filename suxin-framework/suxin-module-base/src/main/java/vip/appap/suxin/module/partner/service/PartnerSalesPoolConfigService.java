package vip.appap.suxin.module.partner.service;

import vip.appap.suxin.module.partner.controller.admin.vo.PartnerSalesPoolConfigSaveReqVO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerSalesPoolConfigDO;

import jakarta.validation.Valid;

/**
 * 客户公海配置 Service 接口
 *
 * @author Wanwan
 */
public interface PartnerSalesPoolConfigService {

    /**
     * 获得客户公海配置
     *
     * @return 客户公海配置
     */
    PartnerSalesPoolConfigDO getCustomerPoolConfig();

    /**
     * 保存客户公海配置
     *
     * @param saveReqVO 更新信息
     */
    void saveCustomerPoolConfig(@Valid PartnerSalesPoolConfigSaveReqVO saveReqVO);

}
