package vip.appap.suxin.module.infra.framework.file.config;

import vip.appap.suxin.module.infra.framework.file.core.client.FileClientFactory;
import vip.appap.suxin.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author 书心软件
 */
@Configuration(proxyBeanMethods = false)
public class SuxinFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
