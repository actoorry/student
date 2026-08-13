package vip.appap.suxin.module.sales.framework.waybill.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SalesElectronicWaybillClientConfigTest {

    @Test
    void createsDedicatedRestTemplateWithConfiguredTimeouts() {
        SalesElectronicWaybillProperties properties = new SalesElectronicWaybillProperties();
        properties.setConnectTimeout(3210);
        properties.setReadTimeout(6540);

        RestTemplate restTemplate = new SalesElectronicWaybillClientConfig()
                .salesElectronicWaybillRestTemplate(new RestTemplateBuilder(), properties);

        SimpleClientHttpRequestFactory factory = assertInstanceOf(SimpleClientHttpRequestFactory.class,
                restTemplate.getRequestFactory());
        assertEquals(3210, ReflectionTestUtils.getField(factory, "connectTimeout"));
        assertEquals(6540, ReflectionTestUtils.getField(factory, "readTimeout"));
    }
}
