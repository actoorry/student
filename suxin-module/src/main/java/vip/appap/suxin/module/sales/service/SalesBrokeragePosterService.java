package vip.appap.suxin.module.sales.service;

/**
 * 分销海报 Service 接口
 */
public interface SalesBrokeragePosterService {

    /**
     * 生成分销海报，并返回海报地址
     *
     * @param userId 用户编号
     * @return 海报地址
     */
    String generateBrokeragePoster(Long userId);

}
