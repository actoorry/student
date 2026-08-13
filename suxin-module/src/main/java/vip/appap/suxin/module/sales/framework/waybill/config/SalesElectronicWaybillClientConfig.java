package vip.appap.suxin.module.sales.framework.waybill.config;

import vip.appap.suxin.module.sales.framework.waybill.core.client.SalesElectronicWaybillClient;
import vip.appap.suxin.module.sales.framework.waybill.core.client.impl.SalesElectronicWaybillClientImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 快递100电子面单客户端配置
 *
 * @author 书心软件
 */
@Configuration(proxyBeanMethods = false)
public class SalesElectronicWaybillClientConfig {

    RestTemplate salesElectronicWaybillRestTemplate(RestTemplateBuilder builder,
                                                    SalesElectronicWaybillProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return builder.requestFactory(() -> requestFactory)
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .build();
    }

    @Bean
    public SalesElectronicWaybillClient salesElectronicWaybillClient(
            RestTemplateBuilder builder, SalesElectronicWaybillProperties properties) {
        return new SalesElectronicWaybillClientImpl(salesElectronicWaybillRestTemplate(builder, properties), properties);
    }

}
