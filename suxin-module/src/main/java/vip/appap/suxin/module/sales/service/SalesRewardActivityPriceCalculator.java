package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.sales.api.SalesRewardActivityApi;
import vip.appap.suxin.module.sales.api.dto.SalesRewardActivityMatchRespDTO;
import vip.appap.suxin.module.sales.enums.SalesConditionTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.module.sales.service.SalesPriceCalculatorHelper.formatPrice;

// TODO @puhui999：相关的单测，建议改一改

/**
 * 满减送活动的 {@link SalesPriceCalculator} 实现类
 *
 * @author 书心软件
 */
@Component
@Order(SalesPriceCalculator.ORDER_REWARD_ACTIVITY)
public class SalesRewardActivityPriceCalculator implements SalesPriceCalculator {

    @Resource
    private SalesRewardActivityApi rewardActivityApi;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 0. 只有【普通】订单，才计算该优惠
        if (ObjectUtil.notEqual(result.getType(), SalesOrderTypeEnum.NORMAL.getType())) {
            return;
        }
        // 获得 SKU 对应的满减送活动
        List<SalesRewardActivityMatchRespDTO> rewardActivities = rewardActivityApi.getMatchRewardActivityListBySpuIds(
                convertSet(result.getItems(), SalesPriceCalculateRespBO.OrderItem::getSpuId));
        if (CollUtil.isEmpty(rewardActivities)) {
            return;
        }
        // 处理最新的满减送活动
        if (!rewardActivities.isEmpty()) {
            calculate(param, result, rewardActivities.get(0));
        }
    }

    private void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result,
                           SalesRewardActivityMatchRespDTO rewardActivity) {
        // 1.1 获得满减送的订单项（商品）列表
        List<SalesPriceCalculateRespBO.OrderItem> orderItems = filterMatchActivityOrderItems(result, rewardActivity);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        // 1.2 获得最大匹配的满减送活动的规则
        SalesRewardActivityMatchRespDTO.Rule rule = getMaxMatchRewardActivityRule(rewardActivity, orderItems);
        if (rule == null) {
            SalesPriceCalculatorHelper.addNotMatchPromotion(result, orderItems,
                    rewardActivity.getId(), rewardActivity.getName(), SalesTypeEnum.REWARD_ACTIVITY.getType(),
                    "满减送：" + rewardActivity.getRules().get(0).getDescription());
            return;
        }

        // 2.1 计算可以优惠的金额
        Integer newDiscountPrice = rule.getDiscountPrice();
        // 2.2 计算分摊的优惠金额
        List<Integer> divideDiscountPrices = SalesPriceCalculatorHelper.dividePrice(orderItems, newDiscountPrice);
        // 2.3 计算是否包邮
        if (Boolean.TRUE.equals(rule.getFreeDelivery())) {
            result.setFreeDelivery(true);
        }

        // 3.1 记录使用的优惠劵
        result.setCouponId(param.getCouponId());
        // 3.2 记录优惠明细
        SalesPriceCalculatorHelper.addPromotion(result, orderItems,
                rewardActivity.getId(), rewardActivity.getName(), SalesTypeEnum.REWARD_ACTIVITY.getType(),
                StrUtil.format("满减送：省 {} 元", formatPrice(rule.getDiscountPrice())),
                divideDiscountPrices);
        // 3.3 更新 SKU 优惠金额
        for (int i = 0; i < orderItems.size(); i++) {
            SalesPriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            orderItem.setDiscountPrice(orderItem.getDiscountPrice() + divideDiscountPrices.get(i));
            SalesPriceCalculatorHelper.recountPayPrice(orderItem);
        }
        SalesPriceCalculatorHelper.recountAllPrice(result);

        // 4.1 记录赠送的积分
        if (rule.getPoint() != null && rule.getPoint() > 0) {
            List<Integer> dividePoints = SalesPriceCalculatorHelper.dividePrice(orderItems, rule.getPoint());
            for (int i = 0; i < orderItems.size(); i++) {
                // 商品可能赠送了积分，所以这里要加上
                SalesPriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
                orderItem.setGivePoint(orderItem.getGivePoint() + dividePoints.get(i));
            }
        }
        // 4.2 记录订单是否包邮
        if (Boolean.TRUE.equals(rule.getFreeDelivery())) {
            // 只要满足一个活动包邮那么这单就包邮
            result.setFreeDelivery(true);
        }
        // 4.3 记录赠送的优惠券
        if (CollUtil.isNotEmpty(rule.getGiveCouponTemplateCounts())) {
            for (Map.Entry<Long, Integer> entry : rule.getGiveCouponTemplateCounts().entrySet()) {
                result.getGiveCouponTemplateCounts().put(entry.getKey(),
                        result.getGiveCouponTemplateCounts().getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
    }

    /**
     * 获得满减送的订单项（商品）列表
     *
     * @param result 计算结果
     * @param rewardActivity 满减送活动
     * @return 订单项（商品）列表
     */
    private List<SalesPriceCalculateRespBO.OrderItem> filterMatchActivityOrderItems(SalesPriceCalculateRespBO result,
                                                                                    SalesRewardActivityMatchRespDTO rewardActivity) {
        return filterList(result.getItems(), orderItem -> CollUtil.contains(rewardActivity.getSpuIds(), orderItem.getSpuId()));
    }

    /**
     * 获得最大匹配的满减送活动的规则
     *
     * @param rewardActivity 满减送活动
     * @param orderItems     商品项
     * @return 匹配的活动规则
     */
    private SalesRewardActivityMatchRespDTO.Rule getMaxMatchRewardActivityRule(SalesRewardActivityMatchRespDTO rewardActivity,
                                                                          List<SalesPriceCalculateRespBO.OrderItem> orderItems) {
        // 1. 计算数量和价格
        Integer count = SalesPriceCalculatorHelper.calculateTotalCount(orderItems);
        Integer price = SalesPriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        assert count != null && price != null;

        // 2. 倒序找一个最大优惠的规则
        for (int i = rewardActivity.getRules().size() - 1; i >= 0; i--) {
            SalesRewardActivityMatchRespDTO.Rule rule = rewardActivity.getRules().get(i);
            if (SalesConditionTypeEnum.PRICE.getType().equals(rewardActivity.getConditionType())
                    && price >= rule.getLimit()) {
                return rule;
            }
            if (SalesConditionTypeEnum.COUNT.getType().equals(rewardActivity.getConditionType())
                    && count >= rule.getLimit()) {
                return rule;
            }
        }
        return null;
    }

}
