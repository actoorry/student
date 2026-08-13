package vip.appap.suxin.module.sales.framework.waybill.core.client.impl;

import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.test.core.ut.BaseMockitoUnitTest;
import vip.appap.suxin.module.sales.dal.dataobject.SalesElectronicWaybillAccountDO;
import vip.appap.suxin.module.sales.framework.waybill.config.SalesElectronicWaybillProperties;
import vip.appap.suxin.module.sales.framework.waybill.core.client.dto.*;
import vip.appap.suxin.module.sales.framework.waybill.core.util.SalesElectronicWaybillSignUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 快递100电子面单客户端协议级测试
 *
 * 覆盖：下单成功/业务失败/超时/空运单号、复打、取消、签名公式。
 */
class SalesElectronicWaybillClientImplTest extends BaseMockitoUnitTest {

    @Mock
    private RestTemplate restTemplate;

    private SalesElectronicWaybillClientImpl client;
    private SalesElectronicWaybillProperties properties;
    private SalesElectronicWaybillAccountDO account;

    @BeforeEach
    void setUp() {
        properties = new SalesElectronicWaybillProperties();
        client = new SalesElectronicWaybillClientImpl(restTemplate, properties);
        account = new SalesElectronicWaybillAccountDO();
        account.setId(1L).setKey("testKey").setSecret("testSecret").setPartnerId("P001")
                .setPartnerKey("pk").setPartnerSecret("ps").setNet("net1").setCode("code1").setPartnerName("pn");
    }

    private SalesElectronicWaybillOrderReqDTO orderReq() {
        SalesElectronicWaybillOrderReqDTO req = new SalesElectronicWaybillOrderReqDTO();
        req.setKuaidicom("shunfeng").setOrderId("S20260811001").setTempId("temp1").setPrintType("HTML").setCount(1);
        SalesElectronicWaybillManInfoDTO rec = new SalesElectronicWaybillManInfoDTO();
        rec.setName("张三").setMobile("13800000000").setPrintAddr("上海市普陀区测试路1号");
        SalesElectronicWaybillManInfoDTO send = new SalesElectronicWaybillManInfoDTO();
        send.setName("李四").setMobile("13900000000").setPrintAddr("北京市海淀区测试路2号");
        req.setRecMan(rec).setSendMan(send);
        return req;
    }

    private void mockResponse(String body) {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(body, HttpStatus.OK));
    }

    @Test
    void order_success_returnsWaybill() {
        mockResponse("{\"code\":200,\"data\":{\"kuaidinum\":\"SF123456\",\"kdComOrderNum\":\"KDC1\",\"taskId\":\"T1\",\"label\":\"https://label\"},\"message\":\"ok\"}");
        SalesElectronicWaybillOrderRespDTO resp = client.order(account, orderReq());
        assertEquals("SF123456", resp.getKuaidinum());
        assertEquals("KDC1", resp.getKdComOrderNum());
        assertEquals("T1", resp.getTaskId());
        assertEquals("https://label", resp.getLabel());
    }

    @Test
    void order_businessFailure_throwsServiceException() {
        mockResponse("{\"code\":4001,\"data\":null,\"message\":\"月结账号不存在\"}");
        ServiceException ex = assertThrows(ServiceException.class, () -> client.order(account, orderReq()));
        assertEquals(1_011_004_304, ex.getCode());
        assertTrue(ex.getMessage().contains("月结账号不存在"));
    }

    @Test
    void order_businessFailure_masksMobileFromMessage() {
        mockResponse("{\"code\":4001,\"data\":null,\"message\":\"手机号 13812345678 不合法\"}");
        ServiceException ex = assertThrows(ServiceException.class, () -> client.order(account, orderReq()));
        assertFalse(ex.getMessage().contains("13812345678"));
        assertTrue(ex.getMessage().contains("138****5678"));
    }

    @Test
    void order_timeout_reportsUnknownResult() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("connect timed out"));
        ServiceException ex = assertThrows(ServiceException.class, () -> client.order(account, orderReq()));
        assertEquals(1_011_004_312, ex.getCode());
    }

    @Test
    void order_successButEmptyWaybillNo_throwsServiceException() {
        mockResponse("{\"code\":200,\"data\":{\"kuaidinum\":\"\"},\"message\":\"ok\"}");
        ServiceException ex = assertThrows(ServiceException.class, () -> client.order(account, orderReq()));
        assertEquals(1_011_004_304, ex.getCode());
    }

    @Test
    void order_non2xx_throwsApiError() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR));
        ServiceException ex = assertThrows(ServiceException.class, () -> client.order(account, orderReq()));
        assertEquals(1_011_004_303, ex.getCode());
    }

    @Test
    void order_signFormulaMatchesDemo() {
        // 捕获请求体，校验 sign = MD5(param + t + key + secret) 转大写
        mockResponse("{\"code\":200,\"data\":{\"kuaidinum\":\"SF123456\"},\"message\":\"ok\"}");
        client.order(account, orderReq());

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(properties.getOrderUrl()), eq(HttpMethod.POST), captor.capture(), eq(String.class));

        MultiValueMap<String, String> body = captor.getValue().getBody();
        String key = body.getFirst("key");
        String t = body.getFirst("t");
        String sign = body.getFirst("sign");
        String param = body.getFirst("param");
        String method = body.getFirst("method");
        assertEquals("testKey", key);
        assertEquals("order", method);
        assertTrue(param.contains("\"reorder\":false"));
        assertTrue(param.contains("\"orderId\":\"S20260811001\""));
        assertTrue(param.contains("\"partnerId\":\"P001\""));
        assertTrue(param.contains("\"printType\":\"HTML\""));
        assertEquals(SalesElectronicWaybillSignUtil.printSign(param, t, "testKey", "testSecret"), sign);
    }

    @Test
    void reprint_sendsTaskIdAndNoNewOrder() {
        mockResponse("{\"code\":200,\"data\":{\"label\":\"https://label2\"},\"message\":\"ok\"}");
        SalesElectronicWaybillOrderRespDTO resp = client.reprint(account, new SalesElectronicWaybillReprintReqDTO().setTaskId("T1"));
        assertEquals("https://label2", resp.getLabel());

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(properties.getOrderUrl()), eq(HttpMethod.POST), captor.capture(), eq(String.class));
        String param = captor.getValue().getBody().getFirst("param");
        assertEquals("printOld", captor.getValue().getBody().getFirst("method"));
        assertTrue(param.contains("\"taskId\":\"T1\""));
    }

    @Test
    void cancel_success_returnsTrue() {
        mockResponse("{\"returnCode\":\"200\",\"result\":true,\"message\":\"取消成功\"}");
        SalesElectronicWaybillCancelRespDTO resp = client.cancel(account,
                new SalesElectronicWaybillCancelReqDTO().setKuaidicom("shunfeng").setKuaidinum("SF123456").setOrderId("KDC1"));
        assertTrue(resp.isSuccess());
    }

    @Test
    void cancel_usesOfficialDemoUrlAndCompleteAccountParams() {
        account.setPartnerName("上海测试网点").setCheckMan("C001").setExpType("标准快递");
        mockResponse("{\"returnCode\":\"200\",\"result\":true,\"message\":\"取消成功\"}");

        client.cancel(account, new SalesElectronicWaybillCancelReqDTO()
                .setKuaidicom("shunfeng").setKuaidinum("SF123456").setOrderId("KDC1"));

        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("http://poll.kuaidi100.com/eorderapi.do"), eq(HttpMethod.POST),
                captor.capture(), eq(String.class));
        String param = captor.getValue().getBody().getFirst("param");
        assertTrue(param.contains("\"partnerName\":\"上海测试网点\""));
        assertTrue(param.contains("\"checkMan\":\"C001\""));
        assertTrue(param.contains("\"expType\":\"标准快递\""));
    }

    @Test
    void cancel_returnCode200ButResultFalse_throwsServiceException() {
        mockResponse("{\"returnCode\":\"200\",\"result\":false,\"message\":\"未取消\"}");
        assertThrows(ServiceException.class, () -> client.cancel(account,
                new SalesElectronicWaybillCancelReqDTO().setKuaidicom("shunfeng")
                        .setKuaidinum("SF123456").setOrderId("KDC1")));
    }

    @Test
    void cancel_failure_throwsServiceException() {
        mockResponse("{\"returnCode\":\"500\",\"result\":false,\"message\":\"订单已取件，无法取消\"}");
        ServiceException ex = assertThrows(ServiceException.class, () -> client.cancel(account,
                new SalesElectronicWaybillCancelReqDTO().setKuaidicom("shunfeng").setKuaidinum("SF123456").setOrderId("KDC1")));
        assertEquals(1_011_004_306, ex.getCode());
    }

}
