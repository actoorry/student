package vip.appap.suxin.module.crm.service;

import vip.appap.suxin.module.crm.controller.admin.vo.CrmContractConfigSaveReqVO;
import vip.appap.suxin.module.crm.dal.dataobject.CrmContractConfigDO;
import jakarta.validation.Valid;

/**
 * 合同配置 Service 接口
 *
 * @author 书心软件
 */
public interface CrmContractConfigService {

    /**
     * 获得合同配置
     *
     * @return 合同配置
     */
    CrmContractConfigDO getContractConfig();

    /**
     * 保存合同配置
     *
     * @param saveReqVO 更新信息
     */
    void saveContractConfig(@Valid CrmContractConfigSaveReqVO saveReqVO);

}
