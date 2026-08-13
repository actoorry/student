package vip.appap.suxin.module.report.framework.jmreport.config;

import vip.appap.suxin.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import vip.appap.suxin.framework.common.biz.system.permission.PermissionCommonApi;
import vip.appap.suxin.framework.security.config.SecurityProperties;
import vip.appap.suxin.module.report.framework.jmreport.core.service.JmOnlDragExternalServiceImpl;
import vip.appap.suxin.module.report.framework.jmreport.core.service.JmReportTokenServiceImpl;
import vip.appap.suxin.module.system.api.PermissionApi;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 积木报表 / JimuBI 仪表盘配置类
 *
 * @author 书心软件
 */
@Configuration(proxyBeanMethods = false)
// jmreport=积木报表；drag=JimuBI 仪表盘（/drag/list、/drag/share/view 等）
@ComponentScan(basePackages = {"org.jeecg.modules.jmreport", "org.jeecg.modules.drag"})
public class JmReportConfiguration {

    @Bean
    public JmReportTokenServiceI jmReportTokenService(OAuth2TokenCommonApi oAuth2TokenApi,
                                                      PermissionCommonApi permissionApi,
                                                      SecurityProperties securityProperties) {
        return new JmReportTokenServiceImpl(oAuth2TokenApi, permissionApi, securityProperties);
    }

    @Bean // 暂时注释：可以按需实现后打开
    @Primary
    public JmOnlDragExternalServiceImpl jmOnlDragExternalService2() {
        return new JmOnlDragExternalServiceImpl();
    }

}
