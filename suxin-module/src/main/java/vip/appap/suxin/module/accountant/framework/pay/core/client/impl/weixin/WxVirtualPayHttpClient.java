package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import vip.appap.suxin.framework.common.util.json.JsonUtils;

/**
 * 微信虚拟支付 /xpay 服务端 API 客户端。
 */
public class WxVirtualPayHttpClient {

    public static final String DEFAULT_BASE_URL = "https://api.weixin.qq.com";

    private static final String START_UPLOAD_GOODS_URI = "/xpay/start_upload_goods";
    private static final String QUERY_UPLOAD_GOODS_URI = "/xpay/query_upload_goods";
    private static final String START_PUBLISH_GOODS_URI = "/xpay/start_publish_goods";
    private static final String QUERY_PUBLISH_GOODS_URI = "/xpay/query_publish_goods";
    private static final String QUERY_ORDER_URI = "/xpay/query_order";
    private static final String NOTIFY_PROVIDE_GOODS_URI = "/xpay/notify_provide_goods";

    private final String baseUrl;

    public WxVirtualPayHttpClient() {
        this(DEFAULT_BASE_URL);
    }

    public WxVirtualPayHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public WxVirtualPayUploadGoodsResponse startUploadGoods(String accessToken, WxVirtualPayClientConfig config,
                                                            WxVirtualPayUploadGoodsRequest request) {
        return postForResponse(START_UPLOAD_GOODS_URI, accessToken, config, request,
                WxVirtualPayUploadGoodsResponse.class);
    }

    public WxVirtualPayQueryUploadGoodsResponse queryUploadGoods(String accessToken, WxVirtualPayClientConfig config,
                                                                 WxVirtualPayQueryUploadGoodsRequest request) {
        return postForResponse(QUERY_UPLOAD_GOODS_URI, accessToken, config, request,
                WxVirtualPayQueryUploadGoodsResponse.class);
    }

    public WxVirtualPayPublishGoodsResponse startPublishGoods(String accessToken, WxVirtualPayClientConfig config,
                                                              WxVirtualPayPublishGoodsRequest request) {
        return postForResponse(START_PUBLISH_GOODS_URI, accessToken, config, request,
                WxVirtualPayPublishGoodsResponse.class);
    }

    public WxVirtualPayQueryPublishGoodsResponse queryPublishGoods(String accessToken, WxVirtualPayClientConfig config,
                                                                   WxVirtualPayQueryPublishGoodsRequest request) {
        return postForResponse(QUERY_PUBLISH_GOODS_URI, accessToken, config, request,
                WxVirtualPayQueryPublishGoodsResponse.class);
    }

    public String queryOrder(String accessToken, WxVirtualPayClientConfig config, String bodyJson, String sessionKey) {
        return validateAccessTokenResponse(post(QUERY_ORDER_URI, accessToken, config, bodyJson, sessionKey));
    }

    public String notifyProvideGoods(String accessToken, WxVirtualPayClientConfig config, String bodyJson,
                                     String sessionKey) {
        // 官方 notify_provide_goods 只要求 access_token，不要求 pay_sig / signature。
        String url = StrUtil.format("{}{}?access_token={}",
                baseUrl, NOTIFY_PROVIDE_GOODS_URI, URLUtil.encodeQuery(accessToken));
        String responseJson = HttpRequest.post(url)
                .contentType("application/json")
                .body(bodyJson)
                .execute()
                .body();
        return validateAccessTokenResponse(responseJson);
    }

    public String post(String uri, String accessToken, WxVirtualPayClientConfig config, String bodyJson,
                       String sessionKey) {
        String paySig = WxVirtualPaySignatureUtils.calculatePaySig(uri, bodyJson, config.getAppKey());
        String url = StrUtil.format("{}{}?access_token={}&pay_sig={}",
                baseUrl, uri, URLUtil.encodeQuery(accessToken), paySig);
        if (StrUtil.isNotBlank(sessionKey)) {
            url = url + "&signature=" + WxVirtualPaySignatureUtils.calculateSignature(bodyJson, sessionKey);
        }
        return HttpRequest.post(url)
                .contentType("application/json")
                .body(bodyJson)
                .execute()
                .body();
    }

    private <T extends WxVirtualPayCommonResponse> T postForResponse(String uri, String accessToken,
                                                                     WxVirtualPayClientConfig config, Object request,
                                                                     Class<T> responseClass) {
        String bodyJson = JsonUtils.toJsonString(request);
        String responseJson = post(uri, accessToken, config, bodyJson, null);
        T response = JsonUtils.parseObject(responseJson, responseClass);
        if (response == null || !response.isSuccess()) {
            throw new WxVirtualPayApiException(response != null ? response.getErrcode() : null,
                    response != null ? response.getErrmsg() : "empty response");
        }
        return response;
    }

    private String validateAccessTokenResponse(String responseJson) {
        if (StrUtil.isBlank(responseJson)) {
            return responseJson;
        }
        WxVirtualPayCommonResponse response = JsonUtils.parseObject(responseJson, WxVirtualPayCommonResponse.class);
        if (response != null && Integer.valueOf(40001).equals(response.getErrcode())) {
            throw new WxVirtualPayApiException(response.getErrcode(), response.getErrmsg());
        }
        return responseJson;
    }

}
