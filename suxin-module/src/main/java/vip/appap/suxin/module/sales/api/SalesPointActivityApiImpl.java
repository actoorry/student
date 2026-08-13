package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesPointValidateJoinRespDTO;
import vip.appap.suxin.module.sales.service.SalesPointActivityService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 积分商城活动 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesPointActivityApiImpl implements SalesPointActivityApi {

    @Resource
    private SalesPointActivityService pointActivityService;

    @Override
    public SalesPointValidateJoinRespDTO validateJoinPointActivity(Long activityId, Long skuId, Integer count) {
        return pointActivityService.validateJoinPointActivity(activityId, skuId, count);
    }

    @Override
    public void updatePointStockDecr(Long id, Long skuId, Integer count) {
        pointActivityService.updatePointStockDecr(id, skuId, count);
    }

    @Override
    public void updatePointStockIncr(Long id, Long skuId, Integer count) {
        pointActivityService.updatePointStockIncr(id, skuId, count);
    }

}
