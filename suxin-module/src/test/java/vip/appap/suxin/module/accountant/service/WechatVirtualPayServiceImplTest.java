package vip.appap.suxin.module.accountant.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import cn.hutool.core.io.resource.ResourceUtil;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.accountant.controller.app.vo.AppWechatVirtualPaySubmitReqVO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderDO;
import vip.appap.suxin.module.accountant.dal.dataobject.PayOrderExtensionDO;
import vip.appap.suxin.module.accountant.dal.mysql.PayOrderExtensionMapper;
import vip.appap.suxin.module.accountant.dal.redis.no.PayNoRedisDAO;
import vip.appap.suxin.module.accountant.enums.ErrorCodeConstants;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderWechatVirtualDeliverStatusEnum;
import vip.appap.suxin.module.accountant.enums.PayOrderStatusEnum;
import vip.appap.suxin.module.accountant.framework.pay.config.PayProperties;
import vip.appap.suxin.module.accountant.framework.pay.core.client.dto.order.PayOrderRespDTO;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayApiException;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayClientConfig;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayHttpClient;
import vip.appap.suxin.module.partner.dal.dataobject.SalesOrderItemDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSkuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ProductSkuWechatVirtualStatusEnum;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.sales.dal.dataobject.SalesOrderDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderItemMapper;
import vip.appap.suxin.module.sales.dal.mysql.SalesOrderMapper;
import vip.appap.suxin.module.system.dal.dataobject.SocialUserDO;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import vip.appap.suxin.module.system.service.SocialClientService;
import vip.appap.suxin.module.system.service.SocialUserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WechatVirtualPayServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    @Spy
    private WechatVirtualPayServiceImpl service;

    @Mock
    private PayOrderService payOrderService;
    @Mock
    private PayChannelService payChannelService;
    @Mock
    private PayOrderExtensionMapper payOrderExtensionMapper;
    @Mock
    private PayNoRedisDAO payNoRedisDAO;
    @Mock
    private PayProperties payProperties;
    @Mock
    private SalesOrderMapper salesOrderMapper;
    @Mock
    private SalesOrderItemMapper salesOrderItemMapper;
    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private ProductSpuMapper productSpuMapper;
    @Mock
    private SocialUserService socialUserService;
    @Mock
    @SuppressWarnings("unused")
    private SocialClientService socialClientService;
    @Mock
    private WxVirtualPayHttpClient wxVirtualPayHttpClient;

    @Test
    void getWechatVirtualPayCheckoutProfile_physicalOrder_usesMerchantMode() {
        mockPayOrderAndSalesOrder(100L, 100);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem(200L, 300L)));
        when(productSpuMapper.selectById(200L)).thenReturn(ProductSpuDO.builder()
                .id(200L).type(ProductTypeEnum.ENTITY.getValue()).build());

        String mode = service.getWechatVirtualPayCheckoutProfile(100L, 10L).getMode();

        assertEquals("WECHAT_MERCHANT", mode);
    }

    @Test
    void getWechatVirtualPayResult_waitingOrder_queriesWechatAndConfirmsPayment() {
        PayOrderDO waitingOrder = payOrder(100L).setStatus(PayOrderStatusEnum.WAITING.getStatus()).setPrice(100);
        PayOrderDO paidOrder = payOrder(100L).setStatus(PayOrderStatusEnum.SUCCESS.getStatus()).setPrice(100);
        when(payOrderService.getOrder(10L))
                .thenReturn(waitingOrder, waitingOrder, waitingOrder, paidOrder);
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("P100")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("wx-product-1")
                .wechatVirtualDeliverStatus(0)
                .build();
        when(payOrderExtensionMapper.selectListByOrderId(10L)).thenReturn(List.of(extension));
        when(payChannelService.validPayChannel(1L, PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(channel());
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("access-token");
        when(socialUserService.getSocialUserList(100L, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(List.of(socialUser("session-key")));
        doReturn(wxVirtualPayHttpClient).when(service).createWxVirtualPayHttpClient();
        when(wxVirtualPayHttpClient.queryOrder(eq("access-token"), any(),
                argThat(body -> body.equals("{\"openid\":\"openid-1\",\"env\":0,\"order_id\":\"P100\"}")),
                eq(null))).thenReturn("{\"errcode\":0,\"errmsg\":\"ok\",\"order\":{"
                        + "\"order_id\":\"P100\",\"status\":2,\"paid_fee\":100,"
                        + "\"wx_order_id\":\"WX100\",\"wxpay_order_id\":\"TX100\"}}");
        var result = service.getWechatVirtualPayResult(100L, 10L);

        assertEquals(PayOrderStatusEnum.SUCCESS.getStatus(), result.getPayStatus());
        verify(payOrderService).notifyOrder(eq(30L), any(PayOrderRespDTO.class));
        verify(wxVirtualPayHttpClient, never()).notifyProvideGoods(any(), any(), any(), any());
    }

    @Test
    void getWechatVirtualPayResult_invalidAccessToken_refreshesAndRetries() {
        PayOrderDO waitingOrder = payOrder(100L).setStatus(PayOrderStatusEnum.WAITING.getStatus()).setPrice(100);
        PayOrderDO paidOrder = payOrder(100L).setStatus(PayOrderStatusEnum.SUCCESS.getStatus()).setPrice(100);
        when(payOrderService.getOrder(10L))
                .thenReturn(waitingOrder, waitingOrder, waitingOrder, paidOrder);
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("P100")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("wx-product-1")
                .wechatVirtualDeliverStatus(0)
                .build();
        when(payOrderExtensionMapper.selectListByOrderId(10L)).thenReturn(List.of(extension));
        when(payChannelService.validPayChannel(1L, PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(channel());
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("expired-token");
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true))
                .thenReturn("fresh-token");
        when(socialUserService.getSocialUserList(100L, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(List.of(socialUser("session-key")));
        doReturn(wxVirtualPayHttpClient).when(service).createWxVirtualPayHttpClient();
        when(wxVirtualPayHttpClient.queryOrder(eq("expired-token"), any(), any(), eq(null)))
                .thenThrow(new WxVirtualPayApiException(40001, "invalid credential"));
        when(wxVirtualPayHttpClient.queryOrder(eq("fresh-token"), any(), any(), eq(null)))
                .thenReturn("{\"errcode\":0,\"order\":{"
                        + "\"status\":2,\"paid_fee\":100,\"wxpay_order_id\":\"TX100\"}}");

        var result = service.getWechatVirtualPayResult(100L, 10L);

        assertEquals(PayOrderStatusEnum.SUCCESS.getStatus(), result.getPayStatus());
        verify(wxVirtualPayHttpClient).queryOrder(eq("expired-token"), any(), any(), eq(null));
        verify(wxVirtualPayHttpClient).queryOrder(eq("fresh-token"), any(), any(), eq(null));
    }

    @Test
    void syncPendingWechatVirtualPayOrders_thirdRetryNotifiesProvideGoodsAndMarksDelivered() {
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("P100")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("wx-product-1")
                .wechatVirtualDeliverStatus(PayOrderWechatVirtualDeliverStatusEnum.WAITING.getStatus())
                .channelExtras(Map.of(
                        "wechatVirtualOpenid", "openid-1",
                        "wechatVirtualQueryRetryCount", "2"))
                .build();
        when(payOrderExtensionMapper.selectListByChannelCodeAndWechatVirtualDeliverStatus(
                PayChannelEnum.WX_VIRTUAL_LITE.getCode(),
                PayOrderWechatVirtualDeliverStatusEnum.WAITING.getStatus()))
                .thenReturn(List.of(extension));
        when(payOrderExtensionMapper.updateById(any(PayOrderExtensionDO.class))).thenReturn(1);
        when(payOrderService.getOrder(10L)).thenReturn(
                payOrder(100L).setStatus(PayOrderStatusEnum.SUCCESS.getStatus()).setPrice(100));
        when(payChannelService.validPayChannel(1L, PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(channel());
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("access-token");
        doReturn(wxVirtualPayHttpClient).when(service).createWxVirtualPayHttpClient();
        when(wxVirtualPayHttpClient.queryOrder(eq("access-token"), any(),
                eq("{\"openid\":\"openid-1\",\"env\":0,\"order_id\":\"P100\"}"), eq(null)))
                .thenReturn("{\"errcode\":0,\"order\":{\"status\":2,\"paid_fee\":100}}");
        when(wxVirtualPayHttpClient.notifyProvideGoods(eq("access-token"), any(),
                eq("{\"order_id\":\"P100\",\"env\":0}"), eq(null)))
                .thenReturn("{\"errcode\":0,\"errmsg\":\"ok\"}");

        int count = service.syncPendingWechatVirtualPayOrders();

        assertEquals(1, count);
        verify(payOrderExtensionMapper).updateById(
                org.mockito.ArgumentMatchers.<PayOrderExtensionDO>argThat(update ->
                        "3".equals(update.getChannelExtras().get("wechatVirtualQueryRetryCount"))));
        verify(wxVirtualPayHttpClient).notifyProvideGoods(eq("access-token"), any(),
                eq("{\"order_id\":\"P100\",\"env\":0}"), eq(null));
        verify(payOrderExtensionMapper).updateWechatVirtualDeliveryById(eq(20L), argThat(update ->
                PayOrderWechatVirtualDeliverStatusEnum.DELIVERED.getStatus()
                        .equals(update.getWechatVirtualDeliverStatus())
                        && update.getWechatVirtualDeliverConfirmTime() != null));
    }

    @Test
    void submitWechatVirtualPay_usesFixedSanitizedProtocolFixture() {
        mockPayOrderAndSalesOrder(100L, 19800);
        SalesOrderItemDO item = orderItem(200L, 300L);
        item.setCount(2);
        item.setPrice(9900);
        item.setPayPrice(19800);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(item));
        when(productSpuMapper.selectById(200L)).thenReturn(ProductSpuDO.builder().id(200L)
                .type(ProductTypeEnum.MEMBER.getValue()).status(ProductSpuStatusEnum.ENABLE.getStatus())
                .isSale(true).isWechatMiniappVirtualGoods(true).build());
        when(productSkuMapper.selectById(300L))
                .thenReturn(publishedSku(300L, "sandbox-course-product").setPrice(9900));
        when(payChannelService.validPayChannel(1L, PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(channel("1450575102", 1, "test-app-key-2026"));
        when(payOrderExtensionMapper.selectListByOrderId(10L)).thenReturn(List.of());
        when(payNoRedisDAO.generate(any())).thenReturn("VIRTUAL-SANDBOX-ORDER-001");
        when(socialUserService.getSocialUserList(100L, UserTypeEnum.MEMBER.getValue()))
                .thenReturn(List.of(socialUser("dGVzdC1zZXNzaW9uLWtleQ==")));

        var response = service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1");

        assertEquals("short_series_goods", response.getMode());
        assertEquals(ResourceUtil.readUtf8Str("wechatvirtual/request-virtual-payment-sign-data.json").trim(),
                response.getSignData());
        assertEquals("917af55d2ef558993a9c5ba5e4cfd57fef9d6776cc79fd99ab93dec326e85d28",
                response.getPaySig());
        assertEquals("04376a5c90f8929fb10c17fa236fd959c354c1748716764abc568b21d21c5b94",
                response.getSignature());
        assertFalse(response.getSignData().contains("\"platform\""));
        assertFalse(response.getSignData().contains("\"openid\""));
        String responseJson = JsonUtils.toJsonString(response);
        assertFalse(responseJson.contains("test-app-key-2026"));
        assertFalse(responseJson.contains("dGVzdC1zZXNzaW9uLWtleQ=="));
        assertEquals(Set.of("payOrderId", "payExtensionNo", "mode", "signData", "paySig", "signature"),
                JsonUtils.parseMap(responseJson).keySet());
        verify(payOrderExtensionMapper).insert(org.mockito.ArgumentMatchers.<PayOrderExtensionDO>argThat(extension ->
                "2".equals(extension.getChannelExtras().get("wechatVirtualBuyQuantity"))
                        && "9900".equals(extension.getChannelExtras().get("wechatVirtualGoodsPrice"))));
    }

    @Test
    void notifyGoodsDeliver_parsesSanitizedJsonFixture() {
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("VIRTUAL-SANDBOX-ORDER-001")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("sandbox-course-product")
                .channelExtras(Map.of("wechatVirtualBuyQuantity", "2"))
                .build();
        when(payOrderExtensionMapper.selectByNo("VIRTUAL-SANDBOX-ORDER-001")).thenReturn(extension);
        when(payOrderService.getOrder(10L)).thenReturn(payOrder(100L).setPrice(19800));

        Map<String, Object> result = service.notifyGoodsDeliver(
                ResourceUtil.readUtf8Str("wechatvirtual/xpay-goods-deliver-notify.json"), false);

        assertEquals(0, result.get("ErrCode"));
        verify(payOrderService).notifyOrder(eq(30L), any(PayOrderRespDTO.class));
    }

    @Test
    void notifyGoodsDeliver_parsesSanitizedXmlFixture() {
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("VIRTUAL-SANDBOX-ORDER-001")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("sandbox-course-product")
                .channelExtras(Map.of("wechatVirtualBuyQuantity", "2"))
                .build();
        when(payOrderExtensionMapper.selectByNo("VIRTUAL-SANDBOX-ORDER-001")).thenReturn(extension);
        when(payOrderService.getOrder(10L)).thenReturn(payOrder(100L).setPrice(19800));

        Map<String, Object> result = service.notifyGoodsDeliver(
                ResourceUtil.readUtf8Str("wechatvirtual/xpay-goods-deliver-notify.xml"), true);

        assertEquals(0, result.get("ErrCode"));
        verify(payOrderService).notifyOrder(eq(30L), any(PayOrderRespDTO.class));
    }

    @Test
    void notifyGoodsDeliver_productMismatch_doesNotConfirmPayOrder() {
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("P100")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("wx-product-1")
                .channelExtras(Map.of("wechatVirtualBuyQuantity", "1"))
                .build();
        when(payOrderExtensionMapper.selectByNo("P100")).thenReturn(extension);
        Map<String, Object> body = notifyBody();
        body.put("GoodsInfo", Map.of("ProductId", "wrong-product", "Quantity", 1, "ActualPrice", 100));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.notifyGoodsDeliver(body));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_GOODS_DELIVER_MISMATCH.getCode(), ex.getCode());
        verify(payOrderService, never()).notifyOrder(any(), any());
    }

    @Test
    void submitWechatVirtualPay_wrongUser_rejected() {
        when(payOrderService.getOrder(10L)).thenReturn(payOrder(101L));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_ORDER_NOT_SELF.getCode(), ex.getCode());
    }

    @Test
    void submitWechatVirtualPay_amountMismatch_rejected() {
        when(payOrderService.getOrder(10L)).thenReturn(payOrder(100L));
        when(salesOrderMapper.selectById(1L)).thenReturn(SalesOrderDO.builder()
                .id(1L).userId(100L).payOrderId(10L).payPrice(99).payStatus(false).build());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH.getCode(), ex.getCode());
    }

    @Test
    void submitWechatVirtualPay_unsupportedProductType_rejected() {
        mockPayOrderAndSalesOrder(100L, 100);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem(200L, 300L)));
        when(productSpuMapper.selectById(200L)).thenReturn(ProductSpuDO.builder().id(200L).type(ProductTypeEnum.ENTITY.getValue()).build());

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_UNSUPPORTED_PRODUCT_TYPE.getCode(), ex.getCode());
    }

    @Test
    void submitWechatVirtualPay_unpublishedGoods_rejected() {
        mockPayOrderAndSalesOrder(100L, 100);
        mockOrderItem(ProductTypeEnum.MEMBER.getValue(), ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus(), "wx-product-1");

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_GOODS_NOT_PUBLISHED.getCode(), ex.getCode());
    }

    @Test
    void submitWechatVirtualPay_multipleItems_rejected() {
        mockPayOrderAndSalesOrder(100L, 100);
        SalesOrderItemDO first = orderItem(200L, 300L);
        SalesOrderItemDO second = orderItem(201L, 301L);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(first, second));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_UNSUPPORTED_PRODUCT_TYPE.getCode(), ex.getCode());
    }

    @Test
    void submitWechatVirtualPay_discountedOrder_rejectedBeforeSigning() {
        mockPayOrderAndSalesOrder(100L, 100);
        SalesOrderDO salesOrder = salesOrderMapper.selectById(1L);
        salesOrder.setDiscountPrice(1);
        SalesOrderItemDO item = orderItem(200L, 300L);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(item));
        when(productSpuMapper.selectById(200L)).thenReturn(virtualSpu(200L));
        when(productSkuMapper.selectById(300L)).thenReturn(publishedSku(300L, "wx-product-1"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_AMOUNT_MISMATCH.getCode(), ex.getCode());
        verify(payOrderExtensionMapper, never()).insert(
                org.mockito.ArgumentMatchers.<PayOrderExtensionDO>any());
    }

    @Test
    void submitWechatVirtualPay_missingSessionKey_rejectedBeforeCreatingExtension() {
        mockPayOrderAndSalesOrder(100L, 100);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem(200L, 300L)));
        when(productSpuMapper.selectById(200L)).thenReturn(virtualSpu(200L));
        when(productSkuMapper.selectById(300L)).thenReturn(publishedSku(300L, "wx-product-1"));
        when(payChannelService.validPayChannel(1L, PayChannelEnum.WX_VIRTUAL_LITE.getCode())).thenReturn(channel());
        when(socialUserService.getSocialUserList(100L, UserTypeEnum.MEMBER.getValue())).thenReturn(List.of());

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_SESSION_KEY_NOT_FOUND.getCode(), ex.getCode());
        verify(payOrderExtensionMapper, never()).insert(
                org.mockito.ArgumentMatchers.<PayOrderExtensionDO>any());
    }

    @Test
    void submitWechatVirtualPay_missingSessionKeyField_rejectedBeforeCreatingExtension() {
        mockPayOrderAndSalesOrder(100L, 100);
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem(200L, 300L)));
        when(productSpuMapper.selectById(200L)).thenReturn(virtualSpu(200L));
        when(productSkuMapper.selectById(300L)).thenReturn(publishedSku(300L, "wx-product-1"));
        when(payChannelService.validPayChannel(1L, PayChannelEnum.WX_VIRTUAL_LITE.getCode())).thenReturn(channel());
        when(socialUserService.getSocialUserList(100L, UserTypeEnum.MEMBER.getValue())).thenReturn(List.of(
                new SocialUserDO()
                        .setType(SocialTypeEnum.WECHAT_MINI_PROGRAM.getType())
                        .setOpenid("openid-1")
                        .setRawTokenInfo("{\"openid\":\"openid-1\"}")));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.submitWechatVirtualPay(100L, reqVO(), "127.0.0.1"));

        assertEquals(ErrorCodeConstants.WECHAT_VIRTUAL_PAY_SESSION_KEY_NOT_FOUND.getCode(), ex.getCode());
        verify(payOrderExtensionMapper, never()).insert(
                org.mockito.ArgumentMatchers.<PayOrderExtensionDO>any());
    }

    @Test
    void notifyGoodsDeliver_duplicateNotify_doesNotConfirmPayOrderTwice() {
        PayOrderExtensionDO extension = PayOrderExtensionDO.builder()
                .id(20L).orderId(10L).channelId(30L).no("P100")
                .channelCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .wechatVirtualProductId("wx-product-1")
                .channelExtras(Map.of("wechatVirtualBuyQuantity", "1"))
                .build();
        when(payOrderExtensionMapper.selectByNo("P100")).thenReturn(extension);
        when(payOrderService.getOrder(10L))
                .thenReturn(payOrder(100L))
                .thenReturn(payOrder(100L).setStatus(PayOrderStatusEnum.SUCCESS.getStatus()));

        service.notifyGoodsDeliver(notifyBody());
        service.notifyGoodsDeliver(notifyBody());

        verify(payOrderService).notifyOrder(eq(30L), any(PayOrderRespDTO.class));
    }

    private void mockPayOrderAndSalesOrder(Long userId, Integer tradePayPrice) {
        when(payOrderService.getOrder(10L)).thenReturn(payOrder(userId).setPrice(tradePayPrice));
        when(salesOrderMapper.selectById(1L)).thenReturn(SalesOrderDO.builder()
                .id(1L).userId(userId).payOrderId(10L).payStatus(false)
                .totalPrice(tradePayPrice).payPrice(tradePayPrice)
                .discountPrice(0).deliveryPrice(0).adjustPrice(0)
                .couponPrice(0).usePoint(0).pointPrice(0).vipPrice(0)
                .build());
    }

    private void mockOrderItem(Integer productType, Integer publishStatus, String productId) {
        when(salesOrderItemMapper.selectListByOrderId(1L)).thenReturn(List.of(orderItem(200L, 300L)));
        when(productSpuMapper.selectById(200L)).thenReturn(ProductSpuDO.builder().id(200L).type(productType)
                .status(ProductSpuStatusEnum.ENABLE.getStatus()).isSale(true)
                .isWechatMiniappVirtualGoods(true).build());
        when(productSkuMapper.selectById(300L)).thenReturn(new ProductSkuDO()
                .setId(300L)
                .setSpuId(200L)
                .setPrice(100)
                .setWechatVirtualProductId(productId)
                .setWechatVirtualUploadStatus(publishStatus)
                .setWechatVirtualReviewStatus(publishStatus)
                .setWechatVirtualPublishStatus(publishStatus));
    }

    private PayOrderDO payOrder(Long userId) {
        return PayOrderDO.builder()
                .id(10L)
                .appId(1L)
                .userId(userId)
                .status(PayOrderStatusEnum.WAITING.getStatus())
                .merchantOrderId("1")
                .price(100)
                .build();
    }

    private SalesOrderItemDO orderItem(Long spuId, Long skuId) {
        SalesOrderItemDO item = new SalesOrderItemDO();
        item.setOrderId(1L);
        item.setUserId(100L);
        item.setSpuId(spuId);
        item.setSkuId(skuId);
        item.setCount(1);
        item.setPrice(100);
        item.setPayPrice(100);
        item.setDiscountPrice(0);
        item.setDeliveryPrice(0);
        item.setAdjustPrice(0);
        item.setCouponPrice(0);
        item.setPointPrice(0);
        item.setUsePoint(0);
        item.setVipPrice(0);
        return item;
    }

    private ProductSkuDO publishedSku(Long skuId, String productId) {
        return new ProductSkuDO()
                .setId(skuId)
                .setSpuId(200L)
                .setPrice(100)
                .setWechatVirtualProductId(productId)
                .setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                .setWechatVirtualPublishStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus())
                .setWechatVirtualReviewStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus());
    }

    private ProductSpuDO virtualSpu(Long spuId) {
        return ProductSpuDO.builder().id(spuId).type(ProductTypeEnum.MEMBER.getValue())
                .status(ProductSpuStatusEnum.ENABLE.getStatus()).isSale(true)
                .isWechatMiniappVirtualGoods(true).build();
    }

    private AppWechatVirtualPaySubmitReqVO reqVO() {
        return new AppWechatVirtualPaySubmitReqVO().setPayOrderId(10L).setOpenid("openid-1");
    }

    private Map<String, Object> notifyBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("OutTradeNo", "P100");
        body.put("WeChatPayInfo", Map.of("TransactionId", "WX100", "MchOrderNo", "P100"));
        body.put("GoodsInfo", Map.of("ProductId", "wx-product-1", "Quantity", 1, "ActualPrice", 100));
        body.put("OpenId", "openid-1");
        return body;
    }

    @SuppressWarnings("unused")
    private PayChannelDO channel() {
        return channel("offer-id", 0, "app-key");
    }

    private PayChannelDO channel(String offerId, Integer env, String appKey) {
        return PayChannelDO.builder()
                .id(30L)
                .appId(1L)
                .code(PayChannelEnum.WX_VIRTUAL_LITE.getCode())
                .config(new WxVirtualPayClientConfig()
                        .setAppid("wx-appid")
                        .setOfferId(offerId)
                        .setEnv(env)
                        .setAppKey(appKey))
                .build();
    }

    private SocialUserDO socialUser(String sessionKey) {
        return new SocialUserDO()
                .setType(SocialTypeEnum.WECHAT_MINI_PROGRAM.getType())
                .setOpenid("openid-1")
                .setRawTokenInfo("{\"sessionKey\":\"" + sessionKey + "\"}");
    }

}
