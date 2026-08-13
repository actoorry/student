package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.accountant.api.PayOrderApi;
import vip.appap.suxin.module.accountant.api.dto.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.partner.api.PartnerAddressApi;
import vip.appap.suxin.module.partner.dal.dataobject.SalesCartDO;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.partner.service.PartnerMemberService;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderCreateReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesOrderSettlementReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderItemMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.sales.dal.redis.no.SalesNoRedisDAO;
import vip.appap.suxin.module.sales.enums.SalesDeliveryTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesOrderStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesElectronicWaybillStatusEnum;
import vip.appap.suxin.module.sales.service.bo.SalesPriceCalculateRespBO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesOrderUpdateServiceImplTest extends BaseMockitoUnitTest {

    @Test
    void isWaybillUnknownRetryExpired_onlyExpiresUnknownRecordAfter48Hours() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 12, 10, 0);
        SalesElectronicWaybillDO withinWindow = SalesElectronicWaybillDO.builder()
                .status(SalesElectronicWaybillStatusEnum.UNKNOWN.getStatus()).build();
        withinWindow.setCreateTime(now.minusHours(47));
        SalesElectronicWaybillDO expired = SalesElectronicWaybillDO.builder()
                .status(SalesElectronicWaybillStatusEnum.UNKNOWN.getStatus()).build();
        expired.setCreateTime(now.minusHours(48));

        assertFalse(SalesOrderUpdateServiceImpl.isWaybillUnknownRetryExpired(withinWindow, now));
        assertTrue(SalesOrderUpdateServiceImpl.isWaybillUnknownRetryExpired(expired, now));
    }

    @InjectMocks
    private SalesOrderUpdateServiceImpl service;

    @Mock
    private SalesOrderMapper tradeOrderMapper;
    @Mock
    private SalesOrderItemMapper tradeOrderItemMapper;
    @Mock
    private SalesNoRedisDAO tradeNoRedisDAO;
    @Mock
    private List<SalesOrderHandler> tradeOrderHandlers;
    @Mock
    private SalesCartService salesCartService;
    @Mock
    private SalesPriceService tradePriceService;
    @Mock
    private PayOrderApi payOrderApi;
    @Mock
    private PartnerAddressApi addressApi;
    @Mock
    private PartnerMemberService partnerMemberService;
    @Mock
    private ProductSpuApi productSpuApi;

    @Test
    void updateOrderPaid_duplicateCallbackWithSamePayOrderId_compensatesMembershipActivation() {
        SalesOrderDO paidOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNDELIVERED.getStatus())
                .payStatus(true)
                .payOrderId(10L)
                .build();
        when(tradeOrderMapper.selectById(1L)).thenReturn(paidOrder);
        when(tradeOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of());

        service.updateOrderPaid(1L, 10L);

        verify(payOrderApi, never()).getOrder(anyLong());
        verify(tradeOrderItemMapper).selectListByOrderId(1L);
        verify(partnerMemberService).activateMemberByPaidOrderItems(100L, List.of());
    }

    @Test
    void createOrder_onlineOnlyWithoutDeliveryType_defaultsOnlineAndDoesNotRequireAddress() {
        AppSalesOrderCreateReqVO createReqVO = new AppSalesOrderCreateReqVO();
        createReqVO.setItems(List.of(new AppSalesOrderSettlementReqVO.Item().setSkuId(200L).setCount(1)));
        createReqVO.setPointStatus(false);
        SalesPriceCalculateRespBO calculateRespBO = calculateResp(List.of(SalesDeliveryTypeEnum.ONLINE.getType()), 0);
        when(tradePriceService.calculateOrderPrice(any())).thenReturn(calculateRespBO);
        when(tradeNoRedisDAO.generate(SalesNoRedisDAO.TRADE_ORDER_NO_PREFIX)).thenReturn("T202606220001");

        SalesOrderDO order = service.createOrder(100L, createReqVO);

        assertEquals(SalesDeliveryTypeEnum.ONLINE.getType(), createReqVO.getDeliveryType());
        assertEquals(SalesDeliveryTypeEnum.ONLINE.getType(), order.getDeliveryType());
        verify(addressApi, never()).getAddress(anyLong(), anyLong());
        verify(tradeOrderMapper).insert(order);
    }

    @Test
    void createOrder_autoOnlyWithoutDeliveryType_defaultsAuto() {
        AppSalesOrderCreateReqVO createReqVO = new AppSalesOrderCreateReqVO();
        createReqVO.setItems(List.of(new AppSalesOrderSettlementReqVO.Item().setSkuId(200L).setCount(1)));
        createReqVO.setPointStatus(false);
        SalesPriceCalculateRespBO calculateRespBO = calculateResp(List.of(SalesDeliveryTypeEnum.AUTO.getType()), 0);
        when(tradePriceService.calculateOrderPrice(any())).thenReturn(calculateRespBO);
        when(tradeNoRedisDAO.generate(SalesNoRedisDAO.TRADE_ORDER_NO_PREFIX)).thenReturn("T202606220002");

        SalesOrderDO order = service.createOrder(100L, createReqVO);

        assertEquals(SalesDeliveryTypeEnum.AUTO.getType(), createReqVO.getDeliveryType());
        assertEquals(SalesDeliveryTypeEnum.AUTO.getType(), order.getDeliveryType());
        verify(tradeOrderMapper).insert(order);
    }

    @Test
    void createOrder_cartItem_resolvesOwnedCartAndDeletesItAfterCreate() {
        AppSalesOrderCreateReqVO createReqVO = new AppSalesOrderCreateReqVO();
        createReqVO.setItems(List.of(new AppSalesOrderSettlementReqVO.Item().setCartId(300L)));
        createReqVO.setPointStatus(false);
        SalesCartDO cart = new SalesCartDO()
                .setId(300L)
                .setUserId(100L)
                .setSpuId(10L)
                .setSkuId(200L)
                .setCount(2)
                .setSelected(true);
        SalesPriceCalculateRespBO calculateRespBO =
                calculateResp(List.of(SalesDeliveryTypeEnum.ONLINE.getType()), 0);
        calculateRespBO.getItems().get(0).setCartId(300L).setCount(2);
        when(salesCartService.getCartList(100L, Set.of(300L))).thenReturn(List.of(cart));
        when(tradePriceService.calculateOrderPrice(any())).thenReturn(calculateRespBO);
        when(tradeNoRedisDAO.generate(SalesNoRedisDAO.TRADE_ORDER_NO_PREFIX)).thenReturn("T202606220003");

        SalesOrderDO order = service.createOrder(100L, createReqVO);

        assertEquals(SalesDeliveryTypeEnum.ONLINE.getType(), order.getDeliveryType());
        verify(salesCartService).deleteCart(100L, Set.of(300L));
        verify(tradeOrderMapper).insert(order);
    }

    @Test
    void updateOrderPaid_onlineDelivery_setsDeliveredAndDeliveryTime() {
        SalesOrderDO unpaidOnlineOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(10L)
                .payPrice(100)
                .deliveryType(SalesDeliveryTypeEnum.ONLINE.getType())
                .build();
        PayOrderRespDTO payOrder = new PayOrderRespDTO()
                .setId(10L)
                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId("1")
                .setPrice(100)
                .setChannelCode("wx_pub");
        when(tradeOrderMapper.selectById(1L)).thenReturn(unpaidOnlineOrder);
        when(payOrderApi.getOrder(10L)).thenReturn(payOrder);
        when(tradeOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of());
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                any(SalesOrderDO.class))).thenReturn(1);

        service.updateOrderPaid(1L, 10L);

        ArgumentCaptor<SalesOrderDO> updateCaptor = ArgumentCaptor.forClass(SalesOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                updateCaptor.capture());
        SalesOrderDO updateObj = updateCaptor.getValue();
        assertEquals(SalesOrderStatusEnum.DELIVERED.getStatus(), updateObj.getStatus());
        assertTrue(updateObj.getPayStatus());
        assertNotNull(updateObj.getPayTime());
        assertNotNull(updateObj.getDeliveryTime());
    }

    @Test
    void updateOrderPaid_physicalExpressDelivery_setsUndelivered() {
        SalesOrderDO unpaidPhysicalOrder = SalesOrderDO.builder()
                .id(2L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(20L)
                .payPrice(19900)
                .deliveryType(SalesDeliveryTypeEnum.EXPRESS.getType())
                .build();
        PayOrderRespDTO payOrder = new PayOrderRespDTO()
                .setId(20L)
                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId("2")
                .setPrice(19900)
                .setChannelCode("wx_lite");
        when(tradeOrderMapper.selectById(2L)).thenReturn(unpaidPhysicalOrder);
        when(payOrderApi.getOrder(20L)).thenReturn(payOrder);
        when(tradeOrderItemMapper.selectListByOrderId(2L)).thenReturn(List.of());
        when(tradeOrderMapper.updateByIdAndStatus(eq(2L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                any(SalesOrderDO.class))).thenReturn(1);

        service.updateOrderPaid(2L, 20L);

        ArgumentCaptor<SalesOrderDO> updateCaptor = ArgumentCaptor.forClass(SalesOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(2L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                updateCaptor.capture());
        SalesOrderDO updateObj = updateCaptor.getValue();
        assertEquals(SalesOrderStatusEnum.UNDELIVERED.getStatus(), updateObj.getStatus());
        assertTrue(updateObj.getPayStatus());
        assertNotNull(updateObj.getPayTime());
    }

    @Test
    void updateOrderPaid_memberAutoDelivery_setsCompletedAndFinishTime() {
        SalesOrderDO unpaidOnlineOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(10L)
                .payPrice(100)
                .deliveryType(SalesDeliveryTypeEnum.AUTO.getType())
                .build();
        PayOrderRespDTO payOrder = new PayOrderRespDTO()
                .setId(10L)
                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId("1")
                .setPrice(100)
                .setChannelCode("wx_pub");
        SalesOrderItemDO orderItem = new SalesOrderItemDO();
        orderItem.setSpuId(654L);
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(654L);
        spu.setType(ProductTypeEnum.MEMBER.getValue());
        when(tradeOrderMapper.selectById(1L)).thenReturn(unpaidOnlineOrder);
        when(payOrderApi.getOrder(10L)).thenReturn(payOrder);
        when(tradeOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productSpuApi.getSpu(654L)).thenReturn(spu);
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                any(SalesOrderDO.class))).thenReturn(1);

        service.updateOrderPaid(1L, 10L);

        ArgumentCaptor<SalesOrderDO> updateCaptor = ArgumentCaptor.forClass(SalesOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                updateCaptor.capture());
        SalesOrderDO updateObj = updateCaptor.getValue();
        assertEquals(SalesOrderStatusEnum.COMPLETED.getStatus(), updateObj.getStatus());
        assertNotNull(updateObj.getFinishTime());
    }

    @Test
    void updateOrderPaid_virtualOnlineDelivery_keepsDelivered() {
        SalesOrderDO unpaidOnlineOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(10L)
                .payPrice(100)
                .deliveryType(SalesDeliveryTypeEnum.ONLINE.getType())
                .build();
        PayOrderRespDTO payOrder = new PayOrderRespDTO()
                .setId(10L)
                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId("1")
                .setPrice(100)
                .setChannelCode("wx_pub");
        SalesOrderItemDO orderItem = new SalesOrderItemDO();
        orderItem.setSpuId(655L);
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(655L);
        spu.setType(ProductTypeEnum.SERVICE.getValue());
        when(tradeOrderMapper.selectById(1L)).thenReturn(unpaidOnlineOrder);
        when(payOrderApi.getOrder(10L)).thenReturn(payOrder);
        when(tradeOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productSpuApi.getSpu(655L)).thenReturn(spu);
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                any(SalesOrderDO.class))).thenReturn(1);

        service.updateOrderPaid(1L, 10L);

        ArgumentCaptor<SalesOrderDO> updateCaptor = ArgumentCaptor.forClass(SalesOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                updateCaptor.capture());
        SalesOrderDO updateObj = updateCaptor.getValue();
        assertEquals(SalesOrderStatusEnum.DELIVERED.getStatus(), updateObj.getStatus());
        assertNotNull(updateObj.getDeliveryTime());
    }

    @Test
    void updateOrderPaid_wechatMiniappVirtualAutoDelivery_completesWithoutMemberActivation() {
        SalesOrderDO unpaidOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(10L)
                .payPrice(100)
                .deliveryType(SalesDeliveryTypeEnum.AUTO.getType())
                .build();
        PayOrderRespDTO payOrder = new PayOrderRespDTO()
                .setId(10L)
                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId("1")
                .setPrice(100)
                .setChannelCode("wx_virtual_lite");
        SalesOrderItemDO orderItem = new SalesOrderItemDO();
        orderItem.setSpuId(656L);
        ProductSpuRespDTO spu = new ProductSpuRespDTO();
        spu.setId(656L);
        spu.setType(ProductTypeEnum.SERVICE.getValue());
        spu.setIsWechatMiniappVirtualGoods(true);
        when(tradeOrderMapper.selectById(1L)).thenReturn(unpaidOrder);
        when(payOrderApi.getOrder(10L)).thenReturn(payOrder);
        when(tradeOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productSpuApi.getSpu(656L)).thenReturn(spu);
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                any(SalesOrderDO.class))).thenReturn(1);

        service.updateOrderPaid(1L, 10L);

        ArgumentCaptor<SalesOrderDO> updateCaptor = ArgumentCaptor.forClass(SalesOrderDO.class);
        verify(tradeOrderMapper).updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                updateCaptor.capture());
        assertEquals(SalesOrderStatusEnum.COMPLETED.getStatus(), updateCaptor.getValue().getStatus());
        verify(partnerMemberService).activateMemberByPaidOrderItems(100L, List.of(orderItem));
    }

    @Test
    void updateOrderPaid_membershipFailure_propagatesBeforeOtherPaidHandlers() {
        SalesOrderDO unpaidOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(10L)
                .payPrice(100)
                .deliveryType(SalesDeliveryTypeEnum.AUTO.getType())
                .build();
        PayOrderRespDTO payOrder = new PayOrderRespDTO()
                .setId(10L)
                .setStatus(PayOrderStatusEnum.SUCCESS.getStatus())
                .setMerchantOrderId("1")
                .setPrice(100)
                .setChannelCode("wx_virtual_lite");
        SalesOrderItemDO orderItem = new SalesOrderItemDO().setSpuId(654L);
        ProductSpuRespDTO spu = new ProductSpuRespDTO().setId(654L)
                .setType(ProductTypeEnum.MEMBER.getValue());
        when(tradeOrderMapper.selectById(1L)).thenReturn(unpaidOrder);
        when(payOrderApi.getOrder(10L)).thenReturn(payOrder);
        when(tradeOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem));
        when(productSpuApi.getSpu(654L)).thenReturn(spu);
        when(tradeOrderMapper.updateByIdAndStatus(eq(1L), eq(SalesOrderStatusEnum.UNPAID.getStatus()),
                any(SalesOrderDO.class))).thenReturn(1);
        RuntimeException fulfillmentFailure = new RuntimeException("invalid member duration");
        org.mockito.Mockito.doThrow(fulfillmentFailure).when(partnerMemberService)
                .activateMemberByPaidOrderItems(100L, List.of(orderItem));

        RuntimeException actual = assertThrows(RuntimeException.class,
                () -> service.updateOrderPaid(1L, 10L));

        assertEquals(fulfillmentFailure, actual);
        verify(tradeOrderHandlers, never()).forEach(any());
    }

    @Test
    void updateOrderPaid_clientSuccessWithoutServerPaidOrder_doesNotActivateMembership() {
        SalesOrderDO unpaidOrder = SalesOrderDO.builder()
                .id(1L)
                .userId(100L)
                .status(SalesOrderStatusEnum.UNPAID.getStatus())
                .payStatus(false)
                .payOrderId(10L)
                .payPrice(100)
                .deliveryType(SalesDeliveryTypeEnum.AUTO.getType())
                .build();
        PayOrderRespDTO waitingPayOrder = new PayOrderRespDTO()
                .setId(10L)
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setMerchantOrderId("1")
                .setPrice(100);
        when(tradeOrderMapper.selectById(1L)).thenReturn(unpaidOrder);
        when(payOrderApi.getOrder(10L)).thenReturn(waitingPayOrder);

        assertThrows(ServiceException.class, () -> service.updateOrderPaid(1L, 10L));

        verify(tradeOrderItemMapper, never()).selectListByOrderId(anyLong());
        verify(partnerMemberService, never()).activateMemberByPaidOrderItems(anyLong(), anyList());
        verify(tradeOrderMapper, never()).updateByIdAndStatus(anyLong(), any(), any(SalesOrderDO.class));
    }

    private SalesPriceCalculateRespBO calculateResp(List<Integer> deliveryTypes, Integer payPrice) {
        SalesPriceCalculateRespBO.Price price = new SalesPriceCalculateRespBO.Price()
                .setTotalPrice(payPrice)
                .setDiscountPrice(0)
                .setDeliveryPrice(0)
                .setCouponPrice(0)
                .setPointPrice(0)
                .setVipPrice(0)
                .setPayPrice(payPrice);
        SalesPriceCalculateRespBO.OrderItem item = new SalesPriceCalculateRespBO.OrderItem()
                .setSpuId(10L)
                .setSkuId(200L)
                .setSpuName("online-product")
                .setSelected(true)
                .setCount(1)
                .setPrice(payPrice)
                .setDiscountPrice(0)
                .setDeliveryPrice(0)
                .setCouponPrice(0)
                .setPointPrice(0)
                .setVipPrice(0)
                .setPayPrice(payPrice)
                .setDeliveryTypes(deliveryTypes);
        return new SalesPriceCalculateRespBO().setPrice(price).setItems(List.of(item));
    }

}
