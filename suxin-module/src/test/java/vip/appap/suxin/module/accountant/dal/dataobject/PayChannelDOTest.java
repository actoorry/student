package vip.appap.suxin.module.accountant.dal.dataobject;

import org.junit.jupiter.api.Test;
import vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin.WxVirtualPayClientConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PayChannelDOTest {

    @Test
    void parse_legacyVirtualPayConfigClassName_returnsVirtualConfig() {
        String json = "{\"@class\":\"cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.weixin.WxVirtualPayClientConfig\","
                + "\"appid\":\"wxc6e16a1b05f5ddaa\",\"offerId\":\"1450575102\",\"env\":0,\"appKey\":\"test-app-key\"}";
        PayChannelDO.PayClientConfigTypeHandler handler = new PayChannelDO.PayClientConfigTypeHandler(Object.class);

        Object config = handler.parse(json);

        WxVirtualPayClientConfig virtualConfig = assertInstanceOf(WxVirtualPayClientConfig.class, config);
        assertEquals("wxc6e16a1b05f5ddaa", virtualConfig.getAppid());
        assertEquals("1450575102", virtualConfig.getOfferId());
        assertEquals(0, virtualConfig.getEnv());
        assertEquals("test-app-key", virtualConfig.getAppKey());
    }

}
