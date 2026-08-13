package vip.appap.suxin.module.sales.api;

/**
 * 砍价活动 Api 接口
 *
 * @author HUIHUI
 */
public interface SalesBargainActivityApi {

    /**
     * 更新砍价活动库存
     *
     * @param id 砍价活动编号
     * @param count 购买数量
     */
    void updateBargainActivityStock(Long id, Integer count);

}
