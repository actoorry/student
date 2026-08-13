package vip.appap.suxin.module.sales.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.sales.api.SalesSeckillActivityApi;
import vip.appap.suxin.module.sales.api.dto.SalesSeckillValidateJoinRespDTO;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.service.SalesOrderQueryService;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_SECKILL_TOTAL_LIMIT_COUNT;

// TODO huihui：单测需要补充
/**
 * 秒杀活动的 {@link SalesPriceCalculator} 实现类
 *
 * @author HUIHUI
 */
@Component
@Order(SalesPriceCalculator.ORDER_SECKILL_ACTIVITY)
public class SalesSeckillActivityPriceCalculator implements SalesPriceCalculator {

    @Resource
    private SalesSeckillActivityApi seckillActivityApi;

    @Resource
    private SalesOrderQueryService tradeOrderQueryService;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 1. 判断订单类型和是否具有秒杀活动编号
        if (param.getSeckillActivityId() == null) {
            return;
        }
        Assert.isTrue(param.getItems().size() == 1, "秒杀时，只允许选择一个商品");
        // 2. 校验是否可以参与秒杀
        SalesPriceCalculateRespBO.OrderItem orderItem = result.getItems().get(0);
        SalesSeckillValidateJoinRespDTO seckillActivity = validateJoinSeckill(
                param.getUserId(), param.getSeckillActivityId(),
                orderItem.getSkuId(), orderItem.getCount());

        // 3.1 记录优惠明细
        Integer discountPrice = orderItem.getPayPrice() - seckillActivity.getSeckillPrice() * orderItem.getCount();
        SalesPriceCalculatorHelper.addPromotion(result, orderItem,
                param.getSeckillActivityId(), seckillActivity.getName(), SalesTypeEnum.SECKILL_ACTIVITY.getType(),
                StrUtil.format("秒杀活动：省 {} 元", SalesPriceCalculatorHelper.formatPrice(discountPrice)),
                discountPrice);
        // 3.2 更新 SKU 优惠金额
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
        SalesPriceCalculatorHelper.recountPayPrice(orderItem);
        SalesPriceCalculatorHelper.recountAllPrice(result);
    }

    private SalesSeckillValidateJoinRespDTO validateJoinSeckill(Long userId, Long activityId, Long skuId, Integer count) {
        // 1. 校验是否可以参与秒杀
        SalesSeckillValidateJoinRespDTO seckillActivity = seckillActivityApi.validateJoinSeckill(activityId, skuId, count);
        // 2. 校验总限购数量，目前只有 trade 有具体下单的数据，需要交给 trade 价格计算使用
        int seckillProductCount = tradeOrderQueryService.getActivityProductCount(userId, activityId, SalesOrderTypeEnum.SECKILL);
        if (seckillProductCount + count > seckillActivity.getTotalLimitCount()) {
            throw exception(PRICE_CALCULATE_SECKILL_TOTAL_LIMIT_COUNT);
        }
        return seckillActivity;
    }

}
