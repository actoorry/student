package vip.appap.suxin.framework.banner.core;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.ClassUtils;

import java.util.concurrent.TimeUnit;

/**
 * 项目启动成功后，提供文档相关的地址
 *
 * @author 书心软件
 */
@Slf4j
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS); // 延迟 1 秒，保证输出到结尾
            log.info("\n----------------------------------------------------------\n\t" +
                            "项目启动成功！\n\t" +
                            "接口文档: \t{} \n\t" +
                            "开发文档: \t{} \n\t" +
                            "视频教程: \t{} \n" +
                            "----------------------------------------------------------",
                    "https://doc.appap.vip/api-doc/",
                    "https://doc.appap.vip",
                    "https://doc.appap.vip/02Yf6M7Qn");

//            // 数据报表
//            if (isNotPresent("vip.appap.suxin.module.report.framework.security.config.SecurityConfiguration")) {
//                System.out.println("[报表模块 suxin-module-report - 已禁用][参考 https://doc.appap.vip/report/ 开启]");
//            }
//            // 工作流
//            if (isNotPresent("vip.appap.suxin.module.bpm.framework.flowable.config.BpmFlowableConfiguration")) {
//                System.out.println("[工作流模块 suxin-module-bpm - 已禁用][参考 https://doc.appap.vip/bpm/ 开启]");
//            }
//            // 商城系统
//            if (isNotPresent("vip.appap.suxin.module.trade.framework.web.config.TradeWebConfiguration")) {
//                System.out.println("[商城系统 suxin-module-mall - 已禁用][参考 https://doc.appap.vip/mall/build/ 开启]");
//            }
//            // ERP 系统
//            if (isNotPresent("vip.appap.suxin.module.erp.framework.web.config.ErpWebConfiguration")) {
//                System.out.println("[ERP 系统 suxin-module-erp - 已禁用][参考 https://doc.appap.vip/erp/build/ 开启]");
//            }
//            // CRM 系统
//            if (isNotPresent("vip.appap.suxin.module.crm.framework.web.config.CrmWebConfiguration")) {
//                System.out.println("[CRM 系统 suxin-module-crm - 已禁用][参考 https://doc.appap.vip/crm/build/ 开启]");
//            }
//            // 微信公众号
//            if (isNotPresent("vip.appap.suxin.module.mp.framework.mp.config.MpConfiguration")) {
//                System.out.println("[微信公众号 suxin-module-mp - 已禁用][参考 https://doc.appap.vip/mp/build/ 开启]");
//            }
//            // 支付平台
//            if (isNotPresent("vip.appap.suxin.module.pay.framework.pay.config.PayConfiguration")) {
//                System.out.println("[支付系统 suxin-module-pay - 已禁用][参考 https://doc.appap.vip/pay/build/ 开启]");
//            }
//            // AI 大模型
//            if (isNotPresent("vip.appap.suxin.module.ai.framework.web.config.AiWebConfiguration")) {
//                System.out.println("[AI 大模型 suxin-module-ai - 已禁用][参考 https://doc.appap.vip/ai/build/ 开启]");
//            }
//            // IoT 物联网
//            if (isNotPresent("vip.appap.suxin.module.iot.framework.web.config.IotWebConfiguration")) {
//                System.out.println("[IoT 物联网 suxin-module-iot - 已禁用][参考 https://doc.appap.vip/iot/build/ 开启]");
//            }
        });
    }

    private static boolean isNotPresent(String className) {
        return !ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }

}
