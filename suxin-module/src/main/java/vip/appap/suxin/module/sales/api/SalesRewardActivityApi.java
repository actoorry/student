package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesRewardActivityMatchRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 满减送活动 API 接口
 *
 * @author 书心软件
 */
public interface SalesRewardActivityApi {

    /**
     * 获得 spuId 商品匹配的的满减送活动列表
     *
     * @param spuIds   SPU 编号
     * @return 满减送活动列表
     */
    List<SalesRewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds);

}
