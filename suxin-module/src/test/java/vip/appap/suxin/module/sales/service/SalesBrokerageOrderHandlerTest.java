package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.api.ProductSkuApi;
import vip.appap.suxin.module.product.api.dto.ProductSkuRespDTO;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBrokerageUserDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.enums.SalesBrokerageRecordBizTypeEnum;
import vip.appap.suxin.module.sales.service.bo.SalesBrokerageAddReqBO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesBrokerageOrderHandlerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private SalesBrokerageOrderHandler handler;

    @Mock
    private PartnerApi partnerApi;
    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private ProductSkuApi productSkuApi;
    @Mock
    private SalesBrokerageRecordService brokerageRecordService;
    @Mock
    private SalesBrokerageUserService brokerageUserService;

    @Test
    void beforeOrderCreate_withValidRelation_fixesBrokerageUserId() {
        // 创建订单时固化当时有效的推广关系
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        when(brokerageUserService.getBrokerageUser(100L))
                .thenReturn(new SalesBrokerageUserDO().setId(100L).setBindUserId(300L));

        handler.beforeOrderCreate(order, List.of());

        assertEquals(300L, order.getBrokerageUserId());
    }

    @Test
    void beforeOrderCreate_withoutRelation_keepsNull() {
        // 无推广关系：订单不固化推广人
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        when(brokerageUserService.getBrokerageUser(100L)).thenReturn(null);

        handler.beforeOrderCreate(order, List.of());

        assertNull(order.getBrokerageUserId());
    }

    @Test
    void afterPayOrder_withoutBrokerageUser_doesNotCreateBrokerage() {
        // 单纯下单支付但无推广归因：不产生佣金
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        order.setBrokerageUserId(null);

        handler.afterPayOrder(order, List.of());

        verify(brokerageRecordService, never()).addBrokerage(anyLong(), any(SalesBrokerageRecordBizTypeEnum.class), anyList());
    }

    @Test
    void afterPayOrder_paidMarriageServiceItemCreatesFirstLevelBrokerage() {
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        order.setBrokerageUserId(300L);

        SalesOrderItemDO item = new SalesOrderItemDO();
        item.setId(900L);
        item.setUserId(100L);
        item.setSpuId(655L);
        item.setSkuId(45L);
        item.setSpuName("线下交友会");
        item.setCount(1);
        item.setPrice(19900);
        item.setPayPrice(19900);

        PartnerRespDTO user = new PartnerRespDTO();
        user.setId(100L);
        user.setNickname("buyer");
        when(partnerApi.getUser(100L)).thenReturn(user);

        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(655L);
        spu.setSubCommissionType(true);
        spu.setName("线下交友会");
        ProductSkuRespDTO sku = new ProductSkuRespDTO();
        sku.setId(45L);
        sku.setSpuId(655L);
        sku.setPrice(19900);
        sku.setFirstBrokeragePrice(5000);
        sku.setSecondBrokeragePrice(0);
        when(productSpuApi.getSpuMap(anyCollection())).thenReturn(Map.of(655L, spu));
        when(productSkuApi.getSkuMap(anyCollection())).thenReturn(Map.of(45L, sku));

        handler.afterPayOrder(order, List.of(item));

        ArgumentCaptor<List<SalesBrokerageAddReqBO>> addListCaptor = ArgumentCaptor.forClass(List.class);
        verify(brokerageRecordService).addBrokerage(eq(100L), eq(SalesBrokerageRecordBizTypeEnum.ORDER),
                addListCaptor.capture());
        assertEquals(1, addListCaptor.getValue().size());
        assertEquals("900", addListCaptor.getValue().get(0).getBizId());
        assertEquals(5000, addListCaptor.getValue().get(0).getFirstFixedPrice());
    }

    @Test
    void afterCancelOrderItem_cancelsMarriageBrokerageRecord() {
        SalesOrderDO order = new SalesOrderDO();
        order.setBrokerageUserId(300L);
        SalesOrderItemDO item = new SalesOrderItemDO();
        item.setId(900L);

        handler.afterCancelOrderItem(order, item);

        verify(brokerageRecordService).cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "900");
    }

    @Test
    void afterCancelOrder_unpaidOrder_doesNotCancelBrokerage() {
        // 未支付订单取消：不产生过佣金，直接返回
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        order.setBrokerageUserId(300L);
        order.setPayStatus(false);
        SalesOrderItemDO item = new SalesOrderItemDO();
        item.setId(900L);

        handler.afterCancelOrder(order, List.of(item));

        verify(brokerageRecordService, never()).cancelBrokerage(any(), any());
    }

    @Test
    void afterCancelOrder_paidOrder_cancelsBrokeragePerItem() {
        // 已支付整单取消：逐订单项冲销佣金
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        order.setBrokerageUserId(300L);
        order.setPayStatus(true);
        SalesOrderItemDO item1 = new SalesOrderItemDO();
        item1.setId(900L);
        item1.setAfterSaleStatus(0); // 未售后
        SalesOrderItemDO item2 = new SalesOrderItemDO();
        item2.setId(901L);
        item2.setAfterSaleStatus(0); // 未售后

        handler.afterCancelOrder(order, List.of(item1, item2));

        verify(brokerageRecordService).cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "900");
        verify(brokerageRecordService).cancelBrokerage(SalesBrokerageRecordBizTypeEnum.ORDER, "901");
    }

    @Test
    void afterCancelOrder_afterSaleItems_skipped() {
        // 售后的订单项已单独冲销，整单取消时跳过
        SalesOrderDO order = new SalesOrderDO();
        order.setUserId(100L);
        order.setBrokerageUserId(300L);
        order.setPayStatus(true);
        SalesOrderItemDO item = new SalesOrderItemDO();
        item.setId(900L);
        item.setAfterSaleStatus(1); // 售后中

        handler.afterCancelOrder(order, List.of(item));

        verify(brokerageRecordService, never()).cancelBrokerage(any(), any());
    }

    @Test
    void afterCancelOrderItem_withoutBrokerageUser_doesNothing() {
        // 无推广归因的订单取消：不冲销
        SalesOrderDO order = new SalesOrderDO();
        order.setBrokerageUserId(null);
        SalesOrderItemDO item = new SalesOrderItemDO();
        item.setId(900L);

        handler.afterCancelOrderItem(order, item);

        verify(brokerageRecordService, never()).cancelBrokerage(any(), any());
    }
}
