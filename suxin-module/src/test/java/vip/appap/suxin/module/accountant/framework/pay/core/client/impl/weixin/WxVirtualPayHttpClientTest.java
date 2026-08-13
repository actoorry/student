package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WxVirtualPayHttpClientTest {

    /**
     * 用于捕获请求参数的测试客户端。
     */
    static class CaptureClient extends WxVirtualPayHttpClient {

        private String capturedUri;
        private String capturedAccessToken;
        private WxVirtualPayClientConfig capturedConfig;
        private String capturedBodyJson;
        private String capturedSessionKey;
        private String mockResponse = "{\"errcode\":0,\"errmsg\":\"ok\"}";

        CaptureClient() {
            super("https://example.test");
        }

        CaptureClient(String mockResponse) {
            super("https://example.test");
            this.mockResponse = mockResponse;
        }

        @Override
        public String post(String uri, String accessToken, WxVirtualPayClientConfig config, String bodyJson,
                           String sessionKey) {
            this.capturedUri = uri;
            this.capturedAccessToken = accessToken;
            this.capturedConfig = config;
            this.capturedBodyJson = bodyJson;
            this.capturedSessionKey = sessionKey;
            return mockResponse;
        }

        public String getCapturedUri() {
            return capturedUri;
        }

        public String getCapturedBodyJson() {
            return capturedBodyJson;
        }

        public String getCapturedPaySig() {
            return WxVirtualPaySignatureUtils.calculatePaySig(capturedUri, capturedBodyJson, capturedConfig.getAppKey());
        }

    }

    @Test
    void post_usesSignedUrlAndExactBody() {
        String body = "{\"openid\": \"xxx\", \"user_ip\": \"127.0.0.1\", \"env\": 0}";
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        CaptureClient client = new CaptureClient();

        client.queryOrder("ACCESS_TOKEN", config, body, "9hAb/NEYUlkaMBEsmFgzig==");

        assertEquals("/xpay/query_order", client.getCapturedUri());
        assertEquals(body, client.getCapturedBodyJson());
        assertEquals("ACCESS_TOKEN", client.capturedAccessToken);
        assertEquals("9hAb/NEYUlkaMBEsmFgzig==", client.capturedSessionKey);
        assertEquals("089d9e8dc5d308977360c4b79ec600a93d736802802a807d634192328032f6c7",
                WxVirtualPaySignatureUtils.calculateSignature(body, "9hAb/NEYUlkaMBEsmFgzig=="));
    }

    @Test
    void queryOrder_invalidAccessToken_throwsTypedException() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        CaptureClient client = new CaptureClient(
                "{\"errcode\":40001,\"errmsg\":\"invalid credential\"}");

        WxVirtualPayApiException exception = assertThrows(WxVirtualPayApiException.class,
                () -> client.queryOrder("EXPIRED_TOKEN", config, "{}", null));

        assertEquals(40001, exception.getErrcode());
        assertEquals("invalid credential", exception.getMessage());
    }

    @Test
    void startUploadGoods_serializesOfficialSampleAndComputesPaySig() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        config.setEnv(0);
        CaptureClient client = new CaptureClient();

        WxVirtualPayUploadItem item = new WxVirtualPayUploadItem();
        item.setId("sku_1001");
        item.setName("测试道具");
        item.setPrice(100);
        item.setRemark("测试备注");
        item.setItemUrl("https://example.com/pic.png");
        WxVirtualPayUploadGoodsRequest request = new WxVirtualPayUploadGoodsRequest();
        request.setUploadItem(List.of(item));
        request.setEnv(0);

        WxVirtualPayUploadGoodsResponse response = client.startUploadGoods("ACCESS_TOKEN", config, request);

        assertTrue(response.isSuccess());
        assertEquals("/xpay/start_upload_goods", client.getCapturedUri());
        String body = client.getCapturedBodyJson();
        assertEquals("{\"upload_item\":[{\"id\":\"sku_1001\",\"name\":\"测试道具\","
                + "\"price\":100,\"remark\":\"测试备注\",\"item_url\":\"https://example.com/pic.png\"}],\"env\":0}", body);
        assertEquals(WxVirtualPaySignatureUtils.calculatePaySig("/xpay/start_upload_goods", body, "12345"),
                client.getCapturedPaySig());
    }

    @Test
    void queryUploadGoods_serializesEnvOnlyAndComputesPaySig() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        config.setEnv(1);
        CaptureClient client = new CaptureClient();

        WxVirtualPayQueryUploadGoodsRequest request = new WxVirtualPayQueryUploadGoodsRequest();
        request.setEnv(1);

        WxVirtualPayQueryUploadGoodsResponse response = client.queryUploadGoods("ACCESS_TOKEN", config, request);

        assertTrue(response.isSuccess());
        assertEquals("/xpay/query_upload_goods", client.getCapturedUri());
        String body = client.getCapturedBodyJson();
        assertEquals("{\"env\":1}", body);
        assertEquals(WxVirtualPaySignatureUtils.calculatePaySig("/xpay/query_upload_goods", body, "12345"),
                client.getCapturedPaySig());
    }

    @Test
    void startPublishGoods_serializesOfficialSampleAndComputesPaySig() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        config.setEnv(0);
        CaptureClient client = new CaptureClient();

        WxVirtualPayPublishItem item = new WxVirtualPayPublishItem();
        item.setId("product_1001");
        WxVirtualPayPublishGoodsRequest request = new WxVirtualPayPublishGoodsRequest();
        request.setPublishItem(List.of(item));
        request.setEnv(0);

        WxVirtualPayPublishGoodsResponse response = client.startPublishGoods("ACCESS_TOKEN", config, request);

        assertTrue(response.isSuccess());
        assertEquals("/xpay/start_publish_goods", client.getCapturedUri());
        String body = client.getCapturedBodyJson();
        assertEquals("{\"publish_item\":[{\"id\":\"product_1001\"}],\"env\":0}", body);
        assertEquals(WxVirtualPaySignatureUtils.calculatePaySig("/xpay/start_publish_goods", body, "12345"),
                client.getCapturedPaySig());
    }

    @Test
    void queryPublishGoods_serializesEnvOnlyAndComputesPaySig() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        config.setEnv(1);
        CaptureClient client = new CaptureClient();

        WxVirtualPayQueryPublishGoodsRequest request = new WxVirtualPayQueryPublishGoodsRequest();
        request.setEnv(1);

        WxVirtualPayQueryPublishGoodsResponse response = client.queryPublishGoods("ACCESS_TOKEN", config, request);

        assertTrue(response.isSuccess());
        assertEquals("/xpay/query_publish_goods", client.getCapturedUri());
        String body = client.getCapturedBodyJson();
        assertEquals("{\"env\":1}", body);
        assertEquals(WxVirtualPaySignatureUtils.calculatePaySig("/xpay/query_publish_goods", body, "12345"),
                client.getCapturedPaySig());
    }

    @Test
    void startUploadGoods_throwsOnOfficialErrorResponse() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        CaptureClient client = new CaptureClient("{\"errcode\":268490003,\"errmsg\":\"签名错误\"}");

        WxVirtualPayUploadItem item = new WxVirtualPayUploadItem();
        item.setId("sku_1001");
        item.setName("测试道具");
        item.setPrice(100);
        item.setRemark("测试备注");
        item.setItemUrl("https://example.com/pic.png");
        WxVirtualPayUploadGoodsRequest request = new WxVirtualPayUploadGoodsRequest();
        request.setUploadItem(List.of(item));
        request.setEnv(0);

        WxVirtualPayApiException ex = assertThrows(WxVirtualPayApiException.class,
                () -> client.startUploadGoods("ACCESS_TOKEN", config, request));
        assertEquals(268490003, ex.getErrcode());
        assertEquals("签名错误", ex.getMessage());
    }

    @Test
    void queryUploadGoods_parsesProcessingResponse() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        CaptureClient client = new CaptureClient(
                "{\"errcode\":0,\"errmsg\":\"ok\",\"upload_item\":[{\"id\":\"sku_1001\","
                        + "\"name\":\"测试道具\",\"price\":100,\"remark\":\"测试备注\","
                        + "\"item_url\":\"https://cdn.example.com/pic.png\",\"upload_status\":0,\"errmsg\":\"\"}],"
                        + "\"status\":1}");

        WxVirtualPayQueryUploadGoodsResponse response = client.queryUploadGoods("ACCESS_TOKEN", config,
                new WxVirtualPayQueryUploadGoodsRequest());

        assertTrue(response.isSuccess());
        assertEquals(1, response.getStatus());
        assertNotNull(response.getUploadItem());
        assertEquals(1, response.getUploadItem().size());
        assertEquals("sku_1001", response.getUploadItem().get(0).getId());
        assertEquals(0, response.getUploadItem().get(0).getUploadStatus());
    }

    @Test
    void queryUploadGoods_parsesSuccessResponse() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        CaptureClient client = new CaptureClient(
                "{\"errcode\":0,\"errmsg\":\"ok\",\"upload_item\":[{\"id\":\"product_1001\","
                        + "\"name\":\"测试道具\",\"price\":100,\"remark\":\"测试备注\","
                        + "\"item_url\":\"https://cdn.example.com/pic.png\",\"upload_status\":2,\"errmsg\":\"\"}],"
                        + "\"status\":3}");

        WxVirtualPayQueryUploadGoodsResponse response = client.queryUploadGoods("ACCESS_TOKEN", config,
                new WxVirtualPayQueryUploadGoodsRequest());

        assertTrue(response.isSuccess());
        assertEquals(3, response.getStatus());
        assertEquals("product_1001", response.getUploadItem().get(0).getId());
        assertEquals(2, response.getUploadItem().get(0).getUploadStatus());
    }

    @Test
    void queryPublishGoods_parsesSuccessResponse() {
        WxVirtualPayClientConfig config = new WxVirtualPayClientConfig();
        config.setAppKey("12345");
        CaptureClient client = new CaptureClient(
                "{\"errcode\":0,\"errmsg\":\"ok\",\"publish_item\":[{\"id\":\"product_1001\","
                        + "\"publish_status\":2,\"errmsg\":\"\"}],\"status\":3}");

        WxVirtualPayQueryPublishGoodsResponse response = client.queryPublishGoods("ACCESS_TOKEN", config,
                new WxVirtualPayQueryPublishGoodsRequest());

        assertTrue(response.isSuccess());
        assertEquals(3, response.getStatus());
        assertEquals("product_1001", response.getPublishItem().get(0).getId());
        assertEquals(2, response.getPublishItem().get(0).getPublishStatus());
    }

}
