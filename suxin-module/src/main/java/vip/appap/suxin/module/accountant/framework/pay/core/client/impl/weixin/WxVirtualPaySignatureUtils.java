package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * 微信虚拟支付签名工具。
 */
public final class WxVirtualPaySignatureUtils {

    public static final String REQUEST_VIRTUAL_PAYMENT_URI = "requestVirtualPayment";

    private WxVirtualPaySignatureUtils() {
    }

    public static String calculatePaySig(String uri, String signData, String appKey) {
        return hmacSha256Hex(appKey, uri + "&" + signData);
    }

    public static String calculateRequestVirtualPaymentPaySig(String signData, String appKey) {
        return calculatePaySig(REQUEST_VIRTUAL_PAYMENT_URI, signData, appKey);
    }

    public static String calculateSignature(String signData, String sessionKey) {
        return hmacSha256Hex(sessionKey, signData);
    }

    private static String hmacSha256Hex(String key, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("计算微信虚拟支付签名失败", ex);
        }
    }

}
