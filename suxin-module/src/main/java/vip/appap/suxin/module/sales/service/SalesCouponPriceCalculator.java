package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.api.SalesCouponApi;
import vip.appap.suxin.module.sales.api.dto.SalesCouponRespDTO;
import vip.appap.suxin.module.sales.enums.SalesDiscountTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesProductScopeEnum;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesCouponStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_COUPON_CAN_NOT_USE;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER;

/**
 * 优惠劵的 {@link SalesPriceCalculator} 实现类
 *
 * @author 书心软件
 */
@Component
@Order(SalesPriceCalculator.ORDER_COUPON)
public class SalesCouponPriceCalculator implements SalesPriceCalculator {

    @Resource
    private SalesCouponApi couponApi;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 只有【普通】订单，才允许使用优惠劵
        if (ObjectUtil.notEqual(result.getType(), SalesOrderTypeEnum.NORMAL.getType())) {
            if (param.getCouponId() != null) {
                throw exception(PRICE_CALCULATE_COUPON_NOT_MATCH_NORMAL_ORDER);
            }
            return;
        }

        // 1.1 加载用户的优惠劵列表
        List<SalesCouponRespDTO> coupons = couponApi.getCouponListByUserId(param.getUserId(), SalesCouponStatusEnum.UNUSED.getStatus());
        coupons.removeIf(coupon -> LocalDateTimeUtils.beforeNow(coupon.getValidEndTime()));
        // 1.2 计算优惠劵的使用条件
        result.setCoupons(calculateCoupons(coupons, result));

        // 2. 校验优惠劵是否可用
        if (param.getCouponId() == null) {
            return;
        }
        SalesPriceCalculateRespBO.Coupon couponBO = CollUtil.findOne(result.getCoupons(), item -> item.getId().equals(param.getCouponId()));
        SalesCouponRespDTO coupon = CollUtil.findOne(coupons, item -> item.getId().equals(param.getCouponId()));
        if (couponBO == null || coupon == null) {
            throw exception(PRICE_CALCULATE_COUPON_CAN_NOT_USE, "优惠劵不存在");
        }
        if (Boolean.FALSE.equals(couponBO.getMatch())) {
            throw exception(PRICE_CALCULATE_COUPON_CAN_NOT_USE, couponBO.getMismatchReason());
        }

        // 3.1 计算可以优惠的金额
        List<SalesPriceCalculateRespBO.OrderItem> orderItems = filterMatchCouponOrderItems(result, coupon);
        Integer totalPayPrice = SalesPriceCalculatorHelper.calculateTotalPayPrice(orderItems);
        Integer couponPrice = getCouponPrice(coupon, totalPayPrice);
        // 3.2 计算分摊的优惠金额
        List<Integer> divideCouponPrices = SalesPriceCalculatorHelper.dividePrice(orderItems, couponPrice);

        // 4.1 记录使用的优惠劵
        result.setCouponId(param.getCouponId());
        // 4.2 记录优惠明细
        SalesPriceCalculatorHelper.addPromotion(result, orderItems,
                param.getCouponId(), couponBO.getName(), SalesTypeEnum.COUPON.getType(),
                StrUtil.format("优惠劵：省 {} 元", SalesPriceCalculatorHelper.formatPrice(couponPrice)),
                divideCouponPrices);
        // 4.3 更新 SKU 优惠金额
        for (int i = 0; i < orderItems.size(); i++) {
            SalesPriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            orderItem.setCouponPrice(divideCouponPrices.get(i));
            SalesPriceCalculatorHelper.recountPayPrice(orderItem);
        }
        SalesPriceCalculatorHelper.recountAllPrice(result);
    }

    /**
     * 计算用户的优惠劵列表（可用 + 不可用）
     *
     * @param coupons 优惠劵
     * @param result 计算结果
     * @return 优惠劵列表
     */
    private List<SalesPriceCalculateRespBO.Coupon> calculateCoupons(List<SalesCouponRespDTO> coupons,
                                                                    SalesPriceCalculateRespBO result) {
        return convertList(coupons, coupon -> {
            SalesPriceCalculateRespBO.Coupon matchCoupon = BeanUtils.toBean(coupon, SalesPriceCalculateRespBO.Coupon.class);
            // 1.1 优惠劵未到使用时间
            if (LocalDateTimeUtils.afterNow(coupon.getValidStartTime())) {
                return matchCoupon.setMatch(false).setMismatchReason("优惠劵未到使用时间");
            }
            // 1.2 优惠劵没有匹配的商品
            List<SalesPriceCalculateRespBO.OrderItem> orderItems = filterMatchCouponOrderItems(result, coupon);
            if (CollUtil.isEmpty(orderItems)) {
                return matchCoupon.setMatch(false).setMismatchReason("优惠劵没有匹配的商品");
            }
            // 1.3 差 %1$,.2f 元可用优惠劵
            Integer totalPayPrice = SalesPriceCalculatorHelper.calculateTotalPayPrice(orderItems);
            if (totalPayPrice < coupon.getUsePrice()) {
                return matchCoupon.setMatch(false)
                        .setMismatchReason(String.format("差 %1$,.2f 元可用优惠劵", (coupon.getUsePrice() - totalPayPrice) / 100D));
            }
            // 1.4 优惠金额超过订单金额
            Integer couponPrice = getCouponPrice(coupon, totalPayPrice);
            if (couponPrice >= totalPayPrice) {
                return matchCoupon.setMatch(false).setMismatchReason("优惠金额超过订单金额");
            }

            // 2. 满足条件
            return matchCoupon.setMatch(true);
        });
    }

    private Integer getCouponPrice(SalesCouponRespDTO coupon, Integer totalPayPrice) {
        if (SalesDiscountTypeEnum.PRICE.getType().equals(coupon.getDiscountType())) { // 减价
            return coupon.getDiscountPrice();
        } else if (SalesDiscountTypeEnum.PERCENT.getType().equals(coupon.getDiscountType())) { // 打折
            int couponPrice = totalPayPrice - (totalPayPrice * coupon.getDiscountPercent() / 100);
            return coupon.getDiscountLimitPrice() == null ? couponPrice
                    : Math.min(couponPrice, coupon.getDiscountLimitPrice()); // 优惠上限
        }
        throw new IllegalArgumentException(String.format("优惠劵(%s) 的优惠类型不正确", coupon));
    }

    /**
     * 获得优惠劵可使用的订单项（商品）列表
     *
     * @param result 计算结果
     * @param coupon 优惠劵
     * @return 订单项（商品）列表
     */
    private List<SalesPriceCalculateRespBO.OrderItem> filterMatchCouponOrderItems(SalesPriceCalculateRespBO result,
                                                                                  SalesCouponRespDTO coupon) {
        Predicate<SalesPriceCalculateRespBO.OrderItem> matchPredicate = SalesPriceCalculateRespBO.OrderItem::getSelected;
        if (SalesProductScopeEnum.SPU.getScope().equals(coupon.getProductScope())) {
            matchPredicate = matchPredicate // 额外加如下条件
                    .and(orderItem -> coupon.getProductScopeValues().contains(orderItem.getSpuId()));
        } else if (SalesProductScopeEnum.CATEGORY.getScope().equals(coupon.getProductScope())) {
            matchPredicate = matchPredicate // 额外加如下条件
                    .and(orderItem -> coupon.getProductScopeValues().contains(orderItem.getCategoryId()));
        }
        return filterList(result.getItems(), matchPredicate);
    }

}
