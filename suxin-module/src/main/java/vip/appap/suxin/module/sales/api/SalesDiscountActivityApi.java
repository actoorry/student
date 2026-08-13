package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesDiscountProductRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 限时折扣 API 接口
 *
 * @author 书心软件
 */
public interface SalesDiscountActivityApi {

    /**
     * 获得 skuId 商品匹配的的限时折扣信息
     *
     * @param skuIds 商品 SKU 编号数组
     * @return 限时折扣信息
     */
    List<SalesDiscountProductRespDTO> getMatchDiscountProductListBySkuIds(Collection<Long> skuIds);

}
