package vip.appap.suxin.module.accountant.controller.app;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.accountant.service.WechatVirtualPayService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AppWechatVirtualPayControllerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private AppWechatVirtualPayController controller;
    @Mock
    private WechatVirtualPayService wechatVirtualPayService;

    @Test
    void notifyGoodsDeliver_jsonReturnsWechatSuccessEnvelope() {
        when(wechatVirtualPayService.notifyGoodsDeliver(anyString(), eq(false)))
                .thenReturn(Map.of("ErrCode", 0, "ErrMsg", "success"));

        ResponseEntity<?> response = controller.notifyGoodsDeliver("{\"OutTradeNo\":\"P100\"}", "application/json");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Map.of("ErrCode", 0, "ErrMsg", "success"), response.getBody());
    }

    @Test
    void notifyGoodsDeliver_xmlReturnsWechatSuccessEnvelope() {
        when(wechatVirtualPayService.notifyGoodsDeliver(anyString(), eq(true)))
                .thenReturn(Map.of("ErrCode", 0, "ErrMsg", "success"));

        ResponseEntity<?> response = controller.notifyGoodsDeliver("<xml><OutTradeNo>P100</OutTradeNo></xml>", "application/xml");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(String.valueOf(response.getBody()).contains("<ErrCode>0</ErrCode>"));
        assertTrue(String.valueOf(response.getBody()).contains("<ErrMsg>success</ErrMsg>"));
    }

}
