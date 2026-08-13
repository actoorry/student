package vip.appap.suxin.module.product.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.accountant.dal.dataobject.PayChannelDO;
import vip.appap.suxin.module.accountant.dal.mysql.PayChannelMapper;
import vip.appap.suxin.module.accountant.enums.PayChannelEnum;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.*;
import vip.appap.suxin.module.accountant.service.ProductWechatVirtualGoodsServiceImpl;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRefreshReqVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsRespVO;
import vip.appap.suxin.module.product.controller.admin.vo.ProductWechatVirtualGoodsSyncReqVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import vip.appap.suxin.module.product.dal.mysql.ProductSkuMapper;
import vip.appap.suxin.module.product.dal.mysql.ProductSpuMapper;
import vip.appap.suxin.module.product.enums.ProductSkuWechatVirtualStatusEnum;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import vip.appap.suxin.module.system.dal.dataobject.SocialClientDO;
import vip.appap.suxin.module.system.dal.mysql.SocialClientMapper;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import vip.appap.suxin.module.system.service.SocialClientService;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProductWechatVirtualGoodsServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    @Spy
    private ProductWechatVirtualGoodsServiceImpl service;

    @Mock
    private ProductSpuMapper productSpuMapper;
    @Mock
    private ProductSkuMapper productSkuMapper;
    @Mock
    private PayChannelMapper payChannelMapper;
    @Mock
    private SocialClientMapper socialClientMapper;
    @Mock
    private SocialClientService socialClientService;
    @Mock
    private WxVirtualPayHttpClient wxVirtualPayHttpClient;

    void mockChannel() {
        lenient().when(service.createWxVirtualPayHttpClient()).thenReturn(wxVirtualPayHttpClient);
        lenient().doAnswer(invocation -> invocation.getArgument(0))
                .when(service).normalizeAndValidateImageUrl(anyString());

        PayChannelDO channel = new PayChannelDO();
        channel.setId(1L);
        channel.setAppId(9L);
        channel.setStatus(CommonStatusEnum.ENABLE.getStatus());
        channel.setCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode());
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppid("wxappid");
        config.setOfferId("1450575102");
        config.setEnv(0);
        config.setAppKey("appkey");
        channel.setConfig(config);
        when(payChannelMapper.selectListByCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(List.of(channel));

        SocialClientDO socialClient = new SocialClientDO();
        socialClient.setClientId("wxappid");
        socialClient.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(socialClientMapper.selectBySocialTypeAndUserType(
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), UserTypeEnum.MEMBER.getValue()))
                .thenReturn(socialClient);

        lenient().when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue()))
                .thenReturn("access_token");
    }

    @Test
    void syncWechatVirtualGoods_startsUploadAndReturnsProcessing() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100,
                "https://oss.example.com/20260730/购物车空 (1).png");
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);
        URI encodedImageUri = URI.create(
                "https://oss.example.com/20260730/%E8%B4%AD%E7%89%A9%E8%BD%A6%E7%A9%BA%20(1).png");
        doCallRealMethod().when(service).normalizeAndValidateImageUrl(anyString());
        doReturn(new ProductWechatVirtualGoodsServiceImpl.ImageResourceMetadata(200, "image/png"))
                .when(service).probeImageResource(encodedImageUri);
        WxVirtualPayUploadGoodsResponse uploadResponse = new WxVirtualPayUploadGoodsResponse();
        uploadResponse.setErrcode(0);
        when(wxVirtualPayHttpClient.startUploadGoods(any(), any(), any()))
                .thenReturn(uploadResponse);

        List<ProductWechatVirtualGoodsRespVO> result = service.syncWechatVirtualGoods(syncReq(1L));

        assertEquals(1, result.size());
        assertEquals("sync", result.get(0).getAction());
        assertTrue(result.get(0).getActionSuccess());
        ArgumentCaptor<WxVirtualPayUploadGoodsRequest> requestCaptor =
                ArgumentCaptor.forClass(WxVirtualPayUploadGoodsRequest.class);
        verify(wxVirtualPayHttpClient).startUploadGoods(any(), any(), requestCaptor.capture());
        assertEquals(encodedImageUri.toASCIIString(),
                requestCaptor.getValue().getUploadItem().get(0).getItemUrl());
        verify(productSkuMapper).updateWechatVirtualGoodsFieldsAndClearReviewFailReason(
                eq(1001L), any(ProductSkuDO.class));
        verify(productSkuMapper, atLeastOnce()).updateWechatVirtualGoodsFields(eq(1001L), any(ProductSkuDO.class));
    }

    @Test
    void syncWechatVirtualGoods_invalidAccessToken_refreshesAndRetries() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://pic.png");
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);
        when(socialClientService.getWxMaAccessToken(UserTypeEnum.MEMBER.getValue(), true))
                .thenReturn("fresh-token");
        when(wxVirtualPayHttpClient.startUploadGoods(eq("access_token"), any(), any()))
                .thenThrow(new WxVirtualPayApiException(40001, "invalid credential"));
        when(wxVirtualPayHttpClient.startUploadGoods(eq("fresh-token"), any(), any()))
                .thenReturn(new WxVirtualPayUploadGoodsResponse());

        List<ProductWechatVirtualGoodsRespVO> result = service.syncWechatVirtualGoods(syncReq(1L));

        assertTrue(result.get(0).getActionSuccess());
        verify(wxVirtualPayHttpClient).startUploadGoods(eq("access_token"), any(), any());
        verify(wxVirtualPayHttpClient).startUploadGoods(eq("fresh-token"), any(), any());
    }

    @Test
    void syncWechatVirtualGoods_multipleSkus_startsSingleBatch() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO first = sku(1001L, 100, "https://pic-1.png");
        ProductSkuDO second = sku(1002L, 200, "https://pic-2.png");
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(first, second));
        when(productSkuMapper.selectById(1001L)).thenReturn(first);
        when(productSkuMapper.selectById(1002L)).thenReturn(second);
        when(wxVirtualPayHttpClient.startUploadGoods(any(), any(), any()))
                .thenReturn(new WxVirtualPayUploadGoodsResponse());

        List<ProductWechatVirtualGoodsRespVO> result = service.syncWechatVirtualGoods(syncReq(1L));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ProductWechatVirtualGoodsRespVO::getActionSuccess));
        ArgumentCaptor<WxVirtualPayUploadGoodsRequest> requestCaptor =
                ArgumentCaptor.forClass(WxVirtualPayUploadGoodsRequest.class);
        verify(wxVirtualPayHttpClient, times(1)).startUploadGoods(any(), any(), requestCaptor.capture());
        assertEquals(List.of("sku_1001", "sku_1002"), requestCaptor.getValue().getUploadItem().stream()
                .map(WxVirtualPayUploadItem::getId)
                .toList());
    }

    @Test
    void normalizeAndValidateImageUrl_rejectsNonSuccessStatus() {
        doReturn(new ProductWechatVirtualGoodsServiceImpl.ImageResourceMetadata(404, "image/png"))
                .when(service).probeImageResource(any(URI.class));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.normalizeAndValidateImageUrl("https://oss.example.com/not-found.png"));

        assertTrue(exception.getMessage().contains("HTTP 404"));
    }

    @Test
    void normalizeAndValidateImageUrl_rejectsUnsupportedContentType() {
        doReturn(new ProductWechatVirtualGoodsServiceImpl.ImageResourceMetadata(200, "text/html"))
                .when(service).probeImageResource(any(URI.class));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.normalizeAndValidateImageUrl("https://oss.example.com/login.png"));

        assertTrue(exception.getMessage().contains("Content-Type"));
    }

    @Test
    void syncWechatVirtualGoods_imagePreflightFails_doesNotCallWechat() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://oss.example.com/not-found.png");
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);
        doThrow(new IllegalArgumentException("图片 GET 返回 HTTP 404"))
                .when(service).normalizeAndValidateImageUrl(anyString());

        List<ProductWechatVirtualGoodsRespVO> result = service.syncWechatVirtualGoods(syncReq(1L));

        assertEquals(1, result.size());
        assertFalse(result.get(0).getActionSuccess());
        assertTrue(result.get(0).getActionMessage().contains("HTTP 404"));
        verify(wxVirtualPayHttpClient, never()).startUploadGoods(any(), any(), any());
        verify(productSkuMapper).updateWechatVirtualGoodsFields(eq(1001L), any(ProductSkuDO.class));
    }

    @Test
    void syncWechatVirtualGoods_duplicateWhileProcessing_rejects() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://pic.png");
        sku.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));

        List<ProductWechatVirtualGoodsRespVO> result = service.syncWechatVirtualGoods(syncReq(1L));

        assertEquals(1, result.size());
        assertFalse(result.get(0).getActionSuccess());
        assertTrue(result.get(0).getActionMessage().contains("处理中"));
        verify(wxVirtualPayHttpClient, never()).startUploadGoods(any(), any(), any());
    }

    @Test
    void refreshWechatVirtualGoods_uploadSucceeds_savesProductId() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://pic.png");
        sku.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);

        WxVirtualPayQueryUploadGoodsResponse response = new WxVirtualPayQueryUploadGoodsResponse();
        response.setErrcode(0);
        response.setStatus(3);
        WxVirtualPayQueryUploadGoodsResponse.UploadItemResult item = new WxVirtualPayQueryUploadGoodsResponse.UploadItemResult();
        item.setId("sku_1001");
        item.setUploadStatus(2);
        response.setUploadItem(List.of(item));
        when(wxVirtualPayHttpClient.queryUploadGoods(any(), any(), any())).thenReturn(response);

        List<ProductWechatVirtualGoodsRespVO> result = service.refreshWechatVirtualGoods(refreshReq(1L));

        assertEquals(1, result.size());
        assertTrue(result.get(0).getActionSuccess());
        assertTrue(result.get(0).getActionMessage().contains("上传成功"));
        verify(productSkuMapper).updateWechatVirtualGoodsFieldsAndClearReviewFailReason(
                eq(1001L), any(ProductSkuDO.class));
    }

    @Test
    void refreshWechatVirtualGoods_uploadFailed_savesReason() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://pic.png");
        sku.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);

        WxVirtualPayQueryUploadGoodsResponse response = new WxVirtualPayQueryUploadGoodsResponse();
        response.setErrcode(0);
        response.setStatus(2);
        WxVirtualPayQueryUploadGoodsResponse.UploadItemResult item = new WxVirtualPayQueryUploadGoodsResponse.UploadItemResult();
        item.setId("sku_1001");
        item.setUploadStatus(3);
        item.setErrmsg("图片含有敏感内容");
        response.setUploadItem(List.of(item));
        when(wxVirtualPayHttpClient.queryUploadGoods(any(), any(), any())).thenReturn(response);

        List<ProductWechatVirtualGoodsRespVO> result = service.refreshWechatVirtualGoods(refreshReq(1L));

        assertEquals(1, result.size());
        assertFalse(result.get(0).getActionSuccess());
        assertTrue(result.get(0).getActionMessage().contains("敏感内容"));
    }

    @Test
    void refreshWechatVirtualGoods_multipleSkus_queriesOnceAndMapsAllItems() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO first = sku(1001L, 100, "https://pic-1.png");
        ProductSkuDO second = sku(1002L, 200, "https://pic-2.png");
        first.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        second.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(first, second));
        when(productSkuMapper.selectById(1001L)).thenReturn(first);
        when(productSkuMapper.selectById(1002L)).thenReturn(second);

        WxVirtualPayQueryUploadGoodsResponse response = new WxVirtualPayQueryUploadGoodsResponse();
        response.setStatus(3);
        response.setUploadItem(List.of(uploadResult("sku_1001", 2), uploadResult("sku_1002", 2)));
        when(wxVirtualPayHttpClient.queryUploadGoods(any(), any(), any())).thenReturn(response);

        List<ProductWechatVirtualGoodsRespVO> result = service.refreshWechatVirtualGoods(refreshReq(1L));

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ProductWechatVirtualGoodsRespVO::getActionSuccess));
        verify(wxVirtualPayHttpClient, times(1)).queryUploadGoods(any(), any(), any());
        verify(productSkuMapper).updateWechatVirtualGoodsFieldsAndClearReviewFailReason(eq(1001L), any());
        verify(productSkuMapper).updateWechatVirtualGoodsFieldsAndClearReviewFailReason(eq(1002L), any());
    }

    @Test
    void refreshWechatVirtualGoods_terminalBatchMissingSku_marksItRetryableFailed() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO stale = sku(1001L, 100, "https://pic-1.png");
        ProductSkuDO current = sku(1002L, 200, "https://pic-2.png");
        stale.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        current.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.PROCESSING.getStatus());
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(stale, current));
        when(productSkuMapper.selectById(1001L)).thenReturn(stale);
        when(productSkuMapper.selectById(1002L)).thenReturn(current);

        WxVirtualPayQueryUploadGoodsResponse response = new WxVirtualPayQueryUploadGoodsResponse();
        response.setStatus(3);
        response.setUploadItem(List.of(uploadResult("sku_1002", 2)));
        when(wxVirtualPayHttpClient.queryUploadGoods(any(), any(), any())).thenReturn(response);

        List<ProductWechatVirtualGoodsRespVO> result = service.refreshWechatVirtualGoods(refreshReq(1L));

        assertFalse(result.get(0).getActionSuccess());
        assertTrue(result.get(0).getActionMessage().contains("重新同步"));
        assertTrue(result.get(1).getActionSuccess());
        verify(productSkuMapper).updateWechatVirtualGoodsFields(eq(1001L), argThat(update ->
                Objects.equals(update.getWechatVirtualUploadStatus(),
                        ProductSkuWechatVirtualStatusEnum.FAILED.getStatus())
                        && update.getWechatVirtualUploadTaskId() == null));
        verify(wxVirtualPayHttpClient, times(1)).queryUploadGoods(any(), any(), any());
    }

    @Test
    void publishWechatVirtualGoods_withoutUploadSuccess_rejects() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://pic.png");
        sku.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.FAILED.getStatus());
        sku.setWechatVirtualProductId("product_1001");
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);

        List<ProductWechatVirtualGoodsRespVO> result = service.publishWechatVirtualGoods(syncReq(1L));

        assertEquals(1, result.size());
        assertFalse(result.get(0).getActionSuccess());
        assertTrue(result.get(0).getActionMessage().contains("上传状态不是成功"));
        verify(wxVirtualPayHttpClient, never()).startPublishGoods(any(), any(), any());
    }

    @Test
    void publishWechatVirtualGoods_withProductId_startsPublish() {
        mockChannel();
        ProductSpuDO spu = spu(ProductTypeEnum.SERVICE.getValue());
        ProductSkuDO sku = sku(1001L, 100, "https://pic.png");
        sku.setWechatVirtualUploadStatus(ProductSkuWechatVirtualStatusEnum.SUCCESS.getStatus());
        sku.setWechatVirtualProductId("product_1001");
        when(productSpuMapper.selectById(1L)).thenReturn(spu);
        when(productSkuMapper.selectListBySpuId(1L)).thenReturn(List.of(sku));
        when(productSkuMapper.selectById(1001L)).thenReturn(sku);
        when(wxVirtualPayHttpClient.startPublishGoods(any(), any(), any()))
                .thenReturn(new WxVirtualPayPublishGoodsResponse());

        List<ProductWechatVirtualGoodsRespVO> result = service.publishWechatVirtualGoods(syncReq(1L));

        assertEquals(1, result.size());
        assertTrue(result.get(0).getActionSuccess());
        verify(wxVirtualPayHttpClient).startPublishGoods(any(), any(), any());
    }

    @Test
    void syncWechatVirtualGoods_ambiguousChannels_rejects() {
        PayChannelDO channel1 = channel(1L, "wxappid1");
        PayChannelDO channel2 = channel(2L, "wxappid2");
        when(payChannelMapper.selectListByCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(List.of(channel1, channel2));
        when(productSpuMapper.selectById(1L)).thenReturn(spu(ProductTypeEnum.SERVICE.getValue()));

        assertThrows(RuntimeException.class, () -> service.syncWechatVirtualGoods(syncReq(1L)));
    }

    @Test
    void syncWechatVirtualGoods_channelAppIdMismatch_rejects() {
        PayChannelDO channel = channel(1L, "wxappid1");
        when(payChannelMapper.selectListByCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode()))
                .thenReturn(List.of(channel));
        SocialClientDO socialClient = new SocialClientDO();
        socialClient.setClientId("wxappid2");
        socialClient.setStatus(CommonStatusEnum.ENABLE.getStatus());
        when(socialClientMapper.selectBySocialTypeAndUserType(
                SocialTypeEnum.WECHAT_MINI_PROGRAM.getType(), UserTypeEnum.MEMBER.getValue()))
                .thenReturn(socialClient);
        when(productSpuMapper.selectById(1L)).thenReturn(spu(ProductTypeEnum.SERVICE.getValue()));

        assertThrows(RuntimeException.class, () -> service.syncWechatVirtualGoods(syncReq(1L)));
    }

    // ==================== helpers ====================

    private ProductSpuDO spu(Integer type) {
        ProductSpuDO spu = new ProductSpuDO();
        spu.setId(1L);
        spu.setName("测试 SPU");
        spu.setType(type);
        spu.setIsWechatMiniappVirtualGoods(true);
        return spu;
    }

    private ProductSkuDO sku(Long id, Integer price, String picUrl) {
        ProductSkuDO sku = new ProductSkuDO();
        sku.setId(id);
        sku.setSpuId(1L);
        sku.setPrice(price);
        sku.setPicUrl(picUrl);
        return sku;
    }

    private PayChannelDO channel(Long id, String appid) {
        PayChannelDO channel = new PayChannelDO();
        channel.setId(id);
        channel.setAppId((long) id);
        channel.setStatus(CommonStatusEnum.ENABLE.getStatus());
        channel.setCode(PayChannelEnum.WX_VIRTUAL_LITE.getCode());
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppid(appid);
        config.setOfferId("1450575102");
        config.setEnv(0);
        config.setAppKey("appkey");
        channel.setConfig(config);
        return channel;
    }

    private ProductWechatVirtualGoodsSyncReqVO syncReq(Long spuId) {
        ProductWechatVirtualGoodsSyncReqVO req = new ProductWechatVirtualGoodsSyncReqVO();
        req.setSpuId(spuId);
        return req;
    }

    private ProductWechatVirtualGoodsRefreshReqVO refreshReq(Long spuId) {
        ProductWechatVirtualGoodsRefreshReqVO req = new ProductWechatVirtualGoodsRefreshReqVO();
        req.setSpuId(spuId);
        return req;
    }

    private WxVirtualPayQueryUploadGoodsResponse.UploadItemResult uploadResult(String id, Integer status) {
        WxVirtualPayQueryUploadGoodsResponse.UploadItemResult item =
                new WxVirtualPayQueryUploadGoodsResponse.UploadItemResult();
        item.setId(id);
        item.setUploadStatus(status);
        return item;
    }

}
