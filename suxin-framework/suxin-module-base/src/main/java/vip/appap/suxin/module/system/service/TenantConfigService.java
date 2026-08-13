package vip.appap.suxin.module.system.service;

import vip.appap.suxin.module.system.api.dto.TenantPointTradeConfigRespDTO;

/**
 * 租户积分交易配置 Service 接口
 *
 * @author 书心软件
 */
public interface TenantConfigService {

    /**
     * 获得当前租户的积分交易配置
     *
     * @return 积分交易配置
     */
    TenantPointTradeConfigRespDTO getConfig();

    /**
     * 保存当前租户的积分交易配置
     *
     * @param config 积分交易配置
     */
    void saveConfig(TenantPointTradeConfigRespDTO config);

}
