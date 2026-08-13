package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.BooleanUtil;
import vip.appap.suxin.module.system.api.dto.TenantPointTradeConfigRespDTO;
import vip.appap.suxin.module.system.service.TenantConfigService;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateReqBO;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.filterList;
import static vip.appap.suxin.module.sales.enums.ErrorCodeConstants.PRICE_CALCULATE_POINT_GIVE_OVERFLOW;

/**
 * 赠送积分的 {@link SalesPriceCalculator} 实现类
 *
 * @author owen
 */
@Component
@Order(SalesPriceCalculator.ORDER_POINT_GIVE)
@Slf4j
public class SalesPointGiveCalculator implements SalesPriceCalculator {

    @Resource
    private TenantConfigService tenantConfigService;

    @Override
    public void calculate(SalesPriceCalculateReqBO param, SalesPriceCalculateRespBO result) {
        // 1.1 校验积分功能是否开启
        int givePointPerYuan = Optional.ofNullable(tenantConfigService.getConfig())
                .filter(config -> BooleanUtil.isTrue(config.getPointTradeDeductEnable()))
                .map(TenantPointTradeConfigRespDTO::getPointTradeGivePoint)
                .orElse(0);
        if (givePointPerYuan <= 0) {
            return;
        }
        // 1.2 校验支付金额
        if (result.getPrice().getPayPrice() <= 0) {
            return;
        }

        // 2.1 计算赠送积分（使用 long 防止溢出）
        long givePointLong = (long) result.getPrice().getPayPrice() * givePointPerYuan / 100;
        if (givePointLong > Integer.MAX_VALUE) {
            throw exception(PRICE_CALCULATE_POINT_GIVE_OVERFLOW);
        }
        int givePoint = (int) givePointLong;
        // 2.2 计算分摊的赠送积分
        List<SalesPriceCalculateRespBO.OrderItem> orderItems = filterList(result.getItems(), SalesPriceCalculateRespBO.OrderItem::getSelected);
        List<Integer> dividePoints = SalesPriceCalculatorHelper.dividePrice(orderItems, givePoint);

        // 3.2 更新 SKU 赠送积分
        for (int i = 0; i < orderItems.size(); i++) {
            SalesPriceCalculateRespBO.OrderItem orderItem = orderItems.get(i);
            // 商品可能赠送了积分，所以这里要加上
            orderItem.setGivePoint(orderItem.getGivePoint() + dividePoints.get(i));
        }
        // 3.3 更新订单赠送积分
        SalesPriceCalculatorHelper.recountAllGivePoint(result);
    }

}
