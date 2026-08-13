package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.convert.SalesOrderConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageAddReqBO;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 订单分销的 {@link SalesOrderHandler} 实现类
 *
 * @author 书心软件
 */
@Component
public class SalesBrokerageOrderHandler implements SalesOrderHandler {

    @Resource
    private PartnerApi PartnerApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Resource
    private SalesBrokerageRecordService brokerageRecordService;
    @Resource
    private SalesBrokerageUserService brokerageUserService;

    @Override
    public void beforeOrderCreate(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 设置订单推广人
        SalesBrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(order.getUserId());
        if (brokerageUser != null && brokerageUser.getBindUserId() != null) {
            order.setBrokerageUserId(brokerageUser.getBindUserId());
        }
    }

    @Override
    public void afterPayOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        if (order.getBrokerageUserId() == null) {
            return;
        }
        addBrokerage(order.getUserId(), orderItems);
    }

    @Override
    public void afterCancelOrder(SalesOrderDO order, List<SalesOrderItemDO> orderItems) {
        // 如果是未支付的订单，不会产生分销结果，所以直接 return
        if (!order.getPayStatus()) {
            return;
        }
        if (order.getBrokerageUserId() == null) {
            return;
        }

        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        orderItems.forEach(orderItem -> afterCancelOrderItem(order, orderItem));
    }

    @Override
    public void afterCancelOrderItem(SalesOrderDO order, SalesOrderItemDO orderItem) {
        if (order.getBrokerageUserId() == null) {
            return;
        }
        brokerageRecordService.cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, String.valueOf(orderItem.getId()));
    }

    /**
     * 创建分销记录
     * <p>
     * 目前是支付成功后，就会创建分销记录。
     * <p>
     * 业内还有两种做法，可以根据自己的业务调整：
     * 1. 确认收货后，才创建分销记录
     * 2. 支付 or 下单成功时，创建分销记录（冻结），确认收货解冻或者 n 天后解冻
     *
     * @param userId  用户编号
     * @param orderItems 订单项
     */
    protected void addBrokerage(Long userId, List<SalesOrderItemDO> orderItems) {
        PartnerRespDTO user = PartnerApi.getUser(userId);
        Assert.notNull(user);
        Map<Long, ProductSpuRespDTO> spusMap = productSpuApi.getSpuMap(convertList(orderItems, SalesOrderItemDO::getSpuId));
        Map<Long, ProductSkuRespDTO> skusMap = productSkuApi.getSkuMap(convertList(orderItems, SalesOrderItemDO::getSkuId));

        // 每一个订单项，都会去生成分销记录
        List<SalesBrokerageAddReqBO> addList = convertList(orderItems, item -> {
            ProductSpuRespDTO spu = spusMap.get(item.getSpuId());
            Assert.notNull(spu);
            ProductSkuRespDTO sku = skusMap.get(item.getSkuId());
            Assert.notNull(sku);
            return SalesOrderConvert.INSTANCE.convert(user, item, spu, sku);
        });
        brokerageRecordService.addBrokerage(userId, SalesBrokerageRecordBizTypeEnum.ORDER, addList);
    }

}
