package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesRewardActivityMatchRespDTO;
import vip.appap.suxin.module.sales.service.SalesRewardActivityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 满减送活动 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesRewardActivityApiImpl implements SalesRewardActivityApi {

    @Resource
    private SalesRewardActivityService rewardActivityService;

    @Override
    public List<SalesRewardActivityMatchRespDTO> getMatchRewardActivityListBySpuIds(Collection<Long> spuIds) {
        return rewardActivityService.getMatchRewardActivityListBySpuIds(spuIds);
    }

}
