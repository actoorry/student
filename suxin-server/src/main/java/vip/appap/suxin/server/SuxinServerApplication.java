package vip.appap.suxin.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
 *
 * @author 书心软件
 */
//d1
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${suxin.info.base-package}
@SpringBootApplication(scanBasePackages = {"${suxin.info.base-package}.server", "${suxin.info.base-package}.module"})
public class SuxinServerApplication {

    public static void main(String[] args) {
        // 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章

        SpringApplication.run(SuxinServerApplication.class, args);
//        new SpringApplicationBuilder(SuxinServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

        // 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.appap.vip/quick-start/ 文章
    }

}
