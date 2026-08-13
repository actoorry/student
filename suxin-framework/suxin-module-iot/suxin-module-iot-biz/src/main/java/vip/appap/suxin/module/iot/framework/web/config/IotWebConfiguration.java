package vip.appap.suxin.module.iot.framework.web.config;

import vip.appap.suxin.framework.swagger.config.SuxinSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * iot 模块的 web 组件的 Configuration
 *
 * @author ahh
 */
@Configuration(proxyBeanMethods = false)
public class IotWebConfiguration {

    /**
     * iot 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi iotGroupedOpenApi() {
        return SuxinSwaggerAutoConfiguration.buildGroupedOpenApi("iot");
    }

}
