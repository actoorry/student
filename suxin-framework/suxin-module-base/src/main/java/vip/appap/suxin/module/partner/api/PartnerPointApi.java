package vip.appap.suxin.module.partner.api;

/**
 * 用户积分 API 接口
 */
public interface PartnerPointApi {

    /**
     * 增加用户积分
     */
    void addPoint(Long userId, Integer point, Integer bizType, String bizId);

    /**
     * 扣减用户积分
     */
    void reducePoint(Long userId, Integer point, Integer bizType, String bizId);
}
