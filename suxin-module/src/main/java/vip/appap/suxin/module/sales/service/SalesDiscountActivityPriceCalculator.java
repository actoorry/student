package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.module.partner.api.PartnerLevelApi;
import vip.appap.suxin.module.partner.api.dto.PartnerLevelRespDTO;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.api.SalesDiscountActivityApi;
import vip.appap.suxin.module.sales.api.dto.SalesDiscountProductRespDTO;
import vip.appap.suxin.module.sales.enums.SalesDiscountTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.annotation.Resource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;
import static vip.appap.suxin.framework.common.util.number.MoneyUtils.calculateRatePrice;
import static vip.appap.suxin.module.sales.service.SalesPriceCalculatorHelper.formatPrice;

/**
 * 限时折扣的 {@link SalesPriceCalculator} 实现类
 *
 * 由于"会员折扣"和"限时折扣"是冲突，需要选择优惠金额多的，所以也放在这里计算
 *
 * @author 书心软件
 */
@Component
@Order(SalesPriceCalculator.ORDER_DISCOUNT_ACTIVITY)
public class SalesDiscountActivityPriceCalculator implements SalesPriceCalculator {

    @Resource
    private SalesDiscountActivityApi discountActivityApi;
    @Resource
    private PartnerLevelApi PartnerLevelApi;
    @Resource
    private PartnerApi PartnerApi;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 0. 只有【普通】订单，才计算该优惠
        if (ObjectUtil.notEqual(result.getType(), SalesOrderTypeEnum.NORMAL.getType())) {
            return;
        }

        // 1.1 获得 SKU 对应的限时折扣活动
        List<SalesDiscountProductRespDTO> discountProducts = discountActivityApi.getMatchDiscountProductListBySkuIds(
                convertSet(result.getItems(), SalesPriceCalculateRespBO.OrderItem::getSkuId));
        Map<Long, SalesDiscountProductRespDTO> discountProductMap = convertMap(discountProducts, SalesDiscountProductRespDTO::getSkuId);
        // 1.2 获得会员等级
        PartnerLevelRespDTO level = getMemberLevel(param.getUserId());

        // 2. 计算每个 SKU 的优惠金额
        result.getItems().forEach(orderItem -> {
            if (!orderItem.getSelected()) {
                return;
            }
            // 2.1 计算限时折扣的优惠金额
            SalesDiscountProductRespDTO discountProduct = discountProductMap.get(orderItem.getSkuId());
            Integer discountPrice = calculateActivityPrice(discountProduct, orderItem);
            // 2.2 计算 VIP 优惠金额
            Integer vipPrice = calculateVipPrice(level, orderItem);
            if (discountPrice <= 0 && vipPrice <= 0) {
                return;
            }

            // 3. 选择优惠金额多的
            if (discountPrice > vipPrice) {
                SalesPriceCalculatorHelper.addPromotion(result, orderItem,
                        discountProduct.getActivityId(), discountProduct.getActivityName(), SalesTypeEnum.DISCOUNT_ACTIVITY.getType(),
                        StrUtil.format("限时折扣：省 {} 元", formatPrice(discountPrice)),
                        discountPrice);
                // 更新 SKU 优惠金额
                orderItem.setDiscountPrice(orderItem.getDiscountPrice() + discountPrice);
            } else {
                assert level != null;
                SalesPriceCalculatorHelper.addPromotion(result, orderItem,
                        level.getId(), level.getName(), SalesTypeEnum.MEMBER_LEVEL.getType(),
                        String.format("会员等级折扣：省 %s 元", formatPrice(vipPrice)),
                        vipPrice);
                // 更新 SKU 的优惠金额
                orderItem.setVipPrice(vipPrice);
            }

            // 4. 分摊优惠
            SalesPriceCalculatorHelper.recountPayPrice(orderItem);
            SalesPriceCalculatorHelper.recountAllPrice(result);
        });
    }

    /**
     * 获得用户的等级
     *
     * @param userId 用户编号
     * @return 用户等级
     */
    public PartnerLevelRespDTO getMemberLevel(Long userId) {
        PartnerRespDTO user = PartnerApi.getUser(userId);
        if (user == null || user.getCustomerLevel() == null || user.getCustomerLevel() <= 0) {
            return null;
        }
        return PartnerLevelApi.getMemberLevel(user.getCustomerLevel());
    }

    /**
     * 计算优惠活动的价格
     *
     * @param discount 优惠活动
     * @param orderItem 交易项
     * @return 优惠价格
     */
    public Integer calculateActivityPrice(SalesDiscountProductRespDTO discount,
                                           SalesPriceCalculateRespBO.OrderItem orderItem) {
        if (discount == null) {
            return 0;
        }
        Integer newPrice = orderItem.getPayPrice();
        if (SalesDiscountTypeEnum.PRICE.getType().equals(discount.getDiscountType())) { // 减价
            newPrice -= discount.getDiscountPrice() * orderItem.getCount();
        } else if (SalesDiscountTypeEnum.PERCENT.getType().equals(discount.getDiscountType())) { // 打折
            newPrice = calculateRatePrice(orderItem.getPayPrice(), discount.getDiscountPercent() / 100.0);
        } else {
            throw new IllegalArgumentException(String.format("优惠活动的商品(%s) 的优惠类型不正确", discount));
        }
        return orderItem.getPayPrice() - newPrice;
    }

    /**
     * 计算会员 VIP 的优惠价格
     *
     * @param level 会员等级
     * @param orderItem 交易项
     * @return 优惠价格
     */
    public Integer calculateVipPrice(PartnerLevelRespDTO level,
                                      SalesPriceCalculateRespBO.OrderItem orderItem) {
        if (level == null
                || CommonStatusEnum.isDisable(level.getStatus())
                || level.getDiscountPercent() == null) {
            return 0;
        }
        Integer newPrice = calculateRatePrice(orderItem.getPayPrice(), level.getDiscountPercent().doubleValue());
        return orderItem.getPayPrice() - newPrice;
    }

}
