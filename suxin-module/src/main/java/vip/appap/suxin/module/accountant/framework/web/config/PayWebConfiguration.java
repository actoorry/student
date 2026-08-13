package vip.appap.suxin.module.accountant.framework.web.config;

import vip.appap.suxin.framework.swagger.config.SuxinSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * pay 模块的 web 组件的 Configuration
 *
 * @author 书心软件
 */
@Configuration(proxyBeanMethods = false)
public class PayWebConfiguration {

    /**
     * pay 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi payGroupedOpenApi() {
        return SuxinSwaggerAutoConfiguration.buildGroupedOpenApi("pay");
    }

}

