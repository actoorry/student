package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.module.partner.api.PartnerLevelApi;
import vip.appap.suxin.module.partner.api.PartnerPointApi;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.partner.enums.PartnerExperienceBizTypeEnum;
import vip.appap.suxin.module.partner.enums.PartnerPointBizTypeEnum;
import vip.appap.suxin.module.sales.dal.dataobject.SalesAfterSaleDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.getSumValue;

/**
 * 会员积分、等级的 {@link SalesOrderHandler} 实现类
 *
 * @author owen
 */
@Component
public class SalesPartnerPointOrderHandler implements SalesOrderHandler {

    @Resource
    private PartnerPointApi PartnerPointApi;
    @Resource
    private PartnerLevelApi PartnerLevelApi;

    @Resource
    private SalesAfterSaleService afterSaleService;

    @Override
    public void afterOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 扣减用户积分（订单抵扣）。不在前置扣减的原因，是因为积分扣减时，需要记录关联业务
        reducePoint(order.getUserId(), order.getUsePoint(), PartnerPointBizTypeEnum.ORDER_USE, order.getId());
    }

    @Override
    public void afterPayOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 增加用户积分（订单赠送）
        addPoint(order.getUserId(), order.getGivePoint(), PartnerPointBizTypeEnum.ORDER_GIVE,
                order.getId());

        // 增加用户经验
        PartnerLevelApi.addExperience(order.getUserId(), order.getPayPrice(),
                PartnerExperienceBizTypeEnum.ORDER_GIVE.getType(), String.valueOf(order.getId()));
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }

        // 增加（回滚）用户积分（订单抵扣）
        Integer usePoint = getSumValue(orderItems, SalesOrderItemDO::getUsePoint, Integer::sum);
        addPoint(order.getUserId(), usePoint, PartnerPointBizTypeEnum.ORDER_USE_CANCEL,
                order.getId());

        // 如下的返还，需要经过支持，也就是经历 afterPayOrder 流程
        if (!order.getPayStatus()) {
            return;
        }
        // 扣减（回滚）积分（订单赠送）
        Integer givePoint = getSumValue(orderItems, SalesOrderItemDO::getGivePoint, Integer::sum);
        reducePoint(order.getUserId(), givePoint, PartnerPointBizTypeEnum.ORDER_GIVE_CANCEL,
                order.getId());
        // 扣减（回滚）用户经验
        int payPrice = order.getPayPrice() - order.getRefundPrice();
        PartnerLevelApi.addExperience(order.getUserId(), payPrice,
                PartnerExperienceBizTypeEnum.ORDER_GIVE_CANCEL.getType(), String.valueOf(order.getId()));
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        // 增加（回滚）积分（订单抵扣）
        addPoint(order.getUserId(), orderItem.getUsePoint(), PartnerPointBizTypeEnum.ORDER_USE_CANCEL_ITEM, orderItem.getId());
        // 扣减（回滚）积分（订单赠送）
        reducePoint(order.getUserId(), orderItem.getGivePoint(), PartnerPointBizTypeEnum.ORDER_GIVE_CANCEL_ITEM, orderItem.getId());

        // 扣减（回滚）用户经验
        SalesAfterSaleDO afterSale = afterSaleService.getAfterSale(orderItem.getAfterSaleId());
        PartnerLevelApi.reduceExperience(order.getUserId(), afterSale.getRefundPrice(),
                PartnerExperienceBizTypeEnum.ORDER_GIVE_CANCEL_ITEM.getType(), String.valueOf(orderItem.getId()));
    }

    /**
     * 添加用户积分
     * <p>
     * 目前是支付成功后，就会创建积分记录。
     * <p>
     * 业内还有两种做法，可以根据自己的业务调整：
     * 1. 确认收货后，才创建积分记录
     * 2. 支付 or 下单成功时，创建积分记录（冻结），确认收货解冻或者 n 天后解冻
     *
     * @param userId  用户编号
     * @param point   增加积分数量
     * @param bizType 业务编号
     * @param bizId   业务编号
     */
    protected void addPoint(Long userId, Integer point, PartnerPointBizTypeEnum bizType, Long bizId) {
        if (point != null && point > 0) {
            PartnerPointApi.addPoint(userId, point, bizType.getType(), String.valueOf(bizId));
        }
    }

    protected void reducePoint(Long userId, Integer point, PartnerPointBizTypeEnum bizType, Long bizId) {
        if (point != null && point > 0) {
            PartnerPointApi.reducePoint(userId, point, bizType.getType(), String.valueOf(bizId));
        }
    }

}
