package vip.appap.suxin.module.sales.api;

import vip.appap.suxin.module.sales.api.dto.SalesSeckillValidateJoinRespDTO;
import vip.appap.suxin.module.sales.service.SalesSeckillActivityService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

/**
 * 秒杀活动接口 Api 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesSeckillActivityApiImpl implements SalesSeckillActivityApi {

    @Resource
    private SalesSeckillActivityService activityService;

    @Override
    public void updateSeckillStockDecr(Long id, Long skuId, Integer count) {
        activityService.updateSeckillStockDecr(id, skuId, count);
    }

    @Override
    public void updateSeckillStockIncr(Long id, Long skuId, Integer count) {
        activityService.updateSeckillStockIncr(id, skuId, count);
    }

    @Override
    public SalesSeckillValidateJoinRespDTO validateJoinSeckill(Long activityId, Long skuId, Integer count) {
        return activityService.validateJoinSeckill(activityId, skuId, count);
    }

}
