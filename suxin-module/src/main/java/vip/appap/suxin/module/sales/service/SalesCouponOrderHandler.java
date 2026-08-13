package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.sales.api.SalesCouponApi;
import vip.appap.suxin.module.sales.api.dto.SalesCouponUseReqDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 优惠劵的 {@link SalesOrderHandler} 实现类
 *
 * @author 书心软件
 */
@Component
@Slf4j
public class SalesCouponOrderHandler implements SalesOrderHandler {

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private SalesOrderUpdateService orderUpdateService;
    @Resource
    private SalesOrderQueryService orderQueryService;

    @Resource
    private SalesCouponApi couponApi;

    @Override
    public void afterOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (order.getCouponId() == null || order.getCouponId() <= 0) {
            return;
        }
        // 不在前置扣减的原因，是因为优惠劵要记录使用的订单号
        couponApi.useCoupon(new SalesCouponUseReqDTO().setId(order.getCouponId()).setUserId(order.getUserId())
                .setOrderId(order.getId()));
    }

    @Override
    public void afterPayOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (CollUtil.isEmpty(order.getGiveCouponTemplateCounts())) {
            return;
        }
        // 赠送优惠券
        try {
            List<Long> couponIds = couponApi.takeCouponsByAdmin(order.getGiveCouponTemplateCounts(), order.getUserId());
            if (CollUtil.isEmpty(couponIds)) {
                return;
            }
            orderUpdateService.updateOrderGiveCouponIds(order.getUserId(), order.getId(), couponIds);
        } catch (Exception e) {
            log.error("[afterPayOrder][order({}) 赠送优惠券({})失败，需要手工补偿]", order.getId(), order.getGiveCouponTemplateCounts(), e);
        }
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 情况一：退还订单使用的优惠券
        if (order.getCouponId() != null && order.getCouponId() > 0) {
            // 退回优惠劵
            couponApi.returnUsedCoupon(order.getCouponId());
        }
        // 情况二：收回赠送的优惠券
        if (CollUtil.isEmpty(order.getGiveCouponIds())) {
            return;
        }
        couponApi.invalidateCouponsByAdmin(order.getGiveCouponIds(), order.getUserId());
    }

}
