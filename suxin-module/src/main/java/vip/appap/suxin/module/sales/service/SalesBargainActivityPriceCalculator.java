package vip.appap.suxin.module.sales.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.sales.api.SalesBargainRecordApi;
import vip.appap.suxin.module.sales.api.dto.SalesBargainValidateJoinRespDTO;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

// TODO huihui：单测需要补充
/**
 * 砍价活动的 {@link SalesPriceCalculator} 实现类
 *
 * @author 书心软件
 */
@Component
@Order(SalesPriceCalculator.ORDER_BARGAIN_ACTIVITY)
public class SalesBargainActivityPriceCalculator implements SalesPriceCalculator {

    @Resource
    private SalesBargainRecordApi bargainRecordApi;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 1. 判断订单类型和是否具有拼团记录编号
        if (ObjectUtil.notEqual(result.getType(), SalesOrderTypeEnum.BARGAIN.getType())) {
            return;
        }
        Assert.isTrue(param.getItems().size() == 1, "砍价时，只允许选择一个商品");
        Assert.isTrue(param.getItems().get(0).getCount() == 1, "砍价时，只允许选择一个商品");
        // 2. 校验是否可以参与砍价
        SalesPriceCalculateRespBO.OrderItem orderItem = result.getItems().get(0);
        SalesBargainValidateJoinRespDTO bargainActivity = bargainRecordApi.validateJoinBargain(
                param.getUserId(), param.getBargainRecordId(), orderItem.getSkuId());

        // 3.1 记录优惠明细
        Integer discountPrice = orderItem.getPayPrice() - bargainActivity.getBargainPrice() * orderItem.getCount();
        // TODO 芋艿：极端情况，优惠金额为负数，需要处理
        SalesPriceCalculatorHelper.addPromotion(result, orderItem,
                param.getSeckillActivityId(), bargainActivity.getName(), SalesTypeEnum.BARGAIN_ACTIVITY.getType(),
                StrUtil.format("砍价活动：省 {} 元", SalesPriceCalculatorHelper.formatPrice(discountPrice)),
                discountPrice);
        // 3.2 更新 SKU 优惠金额
        orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
        SalesPriceCalculatorHelper.recountPayPrice(orderItem);
        SalesPriceCalculatorHelper.recountAllPrice(result);
        // 4. 特殊：设置对应的砍价活动编号
        result.setBargainActivityId(bargainActivity.getActivityId());
    }

}
