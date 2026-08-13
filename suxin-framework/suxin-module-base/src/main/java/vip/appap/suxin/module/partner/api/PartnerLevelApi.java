package vip.appap.suxin.module.partner.api;

import vip.appap.suxin.module.partner.api.dto.PartnerLevelRespDTO;

/**
 * 用户等级 API 接口
 */
public interface PartnerLevelApi {

    /**
     * 获得用户等级
     */
    PartnerLevelRespDTO getMemberLevel(Long levelId);

    /**
     * 增加用户经验
     */
    void addExperience(Long userId, Integer price, Integer bizType, String bizId);

    /**
     * 扣减用户经验
     */
    void reduceExperience(Long userId, Integer price, Integer bizType, String bizId);
}
