package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WxVirtualPaySignatureUtilsTest {

    @Test
    void calculatePaySig_serverApi_usesOfficialSample() {
        String body = "{\"openid\": \"xxx\", \"user_ip\": \"127.0.0.1\", \"env\": 0}";

        String paySig = WxVirtualPaySignatureUtils.calculatePaySig("/xpay/query_user_balance", body, "12345");

        assertEquals("c37809f27c6d7fd1837ad2500a04512b66b34fd793a39a385fade56dca89a4b5", paySig);
    }

    @Test
    void calculateSignature_usesOfficialSample() {
        String body = "{\"openid\": \"xxx\", \"user_ip\": \"127.0.0.1\", \"env\": 0}";

        String signature = WxVirtualPaySignatureUtils.calculateSignature(body, "9hAb/NEYUlkaMBEsmFgzig==");

        assertEquals("089d9e8dc5d308977360c4b79ec600a93d736802802a807d634192328032f6c7", signature);
    }

    @Test
    void calculateRequestVirtualPaymentPaySig_usesFixedWxApiUri() {
        String signData = "{\"offerId\":\"1450575102\",\"buyQuantity\":1,\"env\":0}";

        assertEquals(
                WxVirtualPaySignatureUtils.calculatePaySig("requestVirtualPayment", signData, "12345"),
                WxVirtualPaySignatureUtils.calculateRequestVirtualPaymentPaySig(signData, "12345"));
    }

}
