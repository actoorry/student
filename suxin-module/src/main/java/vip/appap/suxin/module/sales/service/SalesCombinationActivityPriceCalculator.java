package vip.appap.suxin.module.sales.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.sales.api.SalesCombinationRecordApi;
import vip.appap.suxin.module.sales.api.dto.SalesCombinationValidateJoinRespDTO;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

// TODO @puhui999：单测可以后补下

/**
 * 拼团活动的 {@link SalesPriceCalculator} 实现类
 *
 * @author HUIHUI
 */
@Component
@Order(SalesPriceCalculator.ORDER_COMBINATION_ACTIVITY)
public class SalesCombinationActivityPriceCalculator implements SalesPriceCalculator {

    @Resource
    private SalesCombinationRecordApi combinationRecordApi;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 1. 判断订单类型和是否具有拼团活动编号
        if (param.getCombinationActivityId() == null) {
            return;
        }
        Assert.isTrue(param.getItems().size() == 1, "拼团时，只允许选择一个商品");
        // 2. 校验是否可以参与拼团
        SalesPriceCalculateRespBO.OrderItem orderItem = result.getItems().get(0);
        SalesCombinationValidateJoinRespDTO combinationActivity = combinationRecordApi.validateJoinCombination(
                param.getUserId(), param.getCombinationActivityId(), param.getCombinationHeadId(),
                orderItem.getSkuId(), orderItem.getCount());

        // 3.1 记录优惠明细
        Integer discountPrice = orderItem.getPayPrice() - combinationActivity.getCombinationPrice() * orderItem.getCount();
        SalesPriceCalculatorHelper.addPromotion(result, orderItem,
                param.getCombinationActivityId(), combinationActivity.getName(), SalesTypeEnum.COMBINATION_ACTIVITY.getType(),
                StrUtil.format("拼团活动：省 {} 元", SalesPriceCalculatorHelper.formatPrice(discountPrice)),
                discountPrice);
        // 3.2 更新 SKU 优惠金额
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
        SalesPriceCalculatorHelper.recountPayPrice(orderItem);
        SalesPriceCalculatorHelper.recountAllPrice(result);
    }

}
