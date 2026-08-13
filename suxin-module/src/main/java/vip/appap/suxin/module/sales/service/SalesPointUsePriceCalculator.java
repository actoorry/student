package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.module.system.api.dto.TenantPointTradeConfigRespDTO;
import vip.appap.suxin.module.system.service.TenantConfigService;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_PAY_PRICE_ILLEGAL;

/**
 * 使用积分的 {@link SalesPriceCalculator} 实现类
 *
 * @author owen
 */
@Component
@Order(SalesPriceCalculator.ORDER_POINT_USE)
@Slf4j
public class SalesPointUsePriceCalculator implements SalesPriceCalculator {

    @Resource
    private TenantConfigService tenantConfigService;
    @Resource
    private PartnerApi PartnerApi;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 判断订单类型是否不为积分商城活动
        if (ObjectUtil.equal(result.getType(), SalesOrderTypeEnum.POINT.getType())) {
            return;
        }
        // 0. 初始化积分
        PartnerRespDTO user = PartnerApi.getUser(param.getUserId());
        result.setTotalPoint(user.getPoint()).setUsePoint(0);

        // 1.1 校验是否使用积分
        if (!BooleanUtil.isTrue(param.getPointStatus())) {
            return;
        }
        // 1.2 校验积分抵扣是否开启
        TenantPointTradeConfigRespDTO config = tenantConfigService.getConfig();
        if (!isDeductPointEnable(config)) {
            return;
        }
        // 1.3 校验用户积分余额
        if (user.getPoint() == null || user.getPoint() <= 0) {
            return;
        }

        // 2.1 计算积分优惠金额（使用 long 防止回绕）
        int pointPrice = calculatePointPrice(config, user.getPoint(), result);
        // 2.2 计算分摊的积分、抵扣金额
        List<SalesPriceCalculateRespBO.OrderItem> orderItems = filterList(result.getItems(), SalesPriceCalculateRespBO.OrderItem::getSelected);
        List<Integer> dividePointPrices = SalesPriceCalculatorHelper.dividePrice(orderItems, pointPrice);
        List<Integer> divideUsePoints = SalesPriceCalculatorHelper.dividePrice(orderItems, result.getUsePoint());

        // 3.1 记录优惠明细
        SalesPriceCalculatorHelper.addPromotion(result, orderItems,
                param.getUserId(), "积分抵扣", SalesTypeEnum.POINT.getType(),
                StrUtil.format("积分抵扣：省 {} 元", SalesPriceCalculatorHelper.formatPrice(pointPrice)),
                dividePointPrices);
        // 3.2 更新 SKU 优惠金额
        for (int i = 0; i < orderItems.size(); i++) {
            SalesPriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            orderItem.setPointPrice(dividePointPrices.get(i));
            orderItem.setUsePoint(divideUsePoints.get(i));
            SalesPriceCalculatorHelper.recountPayPrice(orderItem);
        }
        SalesPriceCalculatorHelper.recountAllPrice(result);
    }

    private boolean isDeductPointEnable(TenantPointTradeConfigRespDTO config) {
        return config != null &&
                BooleanUtil.isTrue(config.getPointTradeDeductEnable()) &&  // 积分功能是否启用
                config.getPointTradeDeductUnitPrice() != null && config.getPointTradeDeductUnitPrice() > 0; // 有没有配置：1 积分抵扣多少分
    }

    private Integer calculatePointPrice(TenantPointTradeConfigRespDTO config, Integer usePoint, SalesPriceCalculateRespBO result) {
        // 每个订单最多可以使用的积分数量
        if (config.getPointTradeDeductMaxPrice() != null && config.getPointTradeDeductMaxPrice() > 0) {
            usePoint = Math.min(usePoint, config.getPointTradeDeductMaxPrice());
        }
        // 积分优惠金额（分），使用 long 防止乘法回绕
        long pointPrice = (long) usePoint * config.getPointTradeDeductUnitPrice();
        if (result.getPrice().getPayPrice() <= pointPrice) {
            // 禁止 0 元购
            throw exception(PRICE_CALCULATE_PAY_PRICE_ILLEGAL);
        }
        // 记录使用的积分
        result.setUsePoint(usePoint);
        return (int) pointPrice;
    }

}
