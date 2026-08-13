package vip.appap.suxin.module.partner.framework.web.config;

import vip.appap.suxin.framework.swagger.config.SuxinSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * member 模块的 web 组件的 Configuration
 *
 * @author 书心软件
 */
@Configuration(proxyBeanMethods = false)
public class PartnerWebConfiguration {

    /**
     * member 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi memberGroupedOpenApi() {
        return SuxinSwaggerAutoConfiguration.buildGroupedOpenApi("member");
    }

}
