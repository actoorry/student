package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesPointValidateJoinRespDTO;

/**
 * 积分商城活动 API 接口
 *
 * @author HUIHUI
 */
public interface SalesPointActivityApi {

    /**
     * 【下单前】校验是否参与积分商城活动
     *
     * 如果校验失败，则抛出业务异常
     *
     * @param activityId 活动编号
     * @param skuId      SKU 编号
     * @param count      数量
     * @return 积分商城商品信息
     */
    SalesPointValidateJoinRespDTO validateJoinPointActivity(Long activityId, Long skuId, Integer count);

    /**
     * 更新积分商城商品库存（减少）
     *
     * @param id    活动编号
     * @param skuId sku 编号
     * @param count 数量(正数)
     */
    void updatePointStockDecr(Long id, Long skuId, Integer count);

    /**
     * 更新积分商城商品库存（增加）
     *
     * @param id    活动编号
     * @param skuId sku 编号
     * @param count 数量(正数)
     */
    void updatePointStockIncr(Long id, Long skuId, Integer count);

}
