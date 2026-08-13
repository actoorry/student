package vip.appap.suxin.module.sales.framework.waybill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 快递100电子面单 V2 配置项
 *
 * 端点默认对齐官方 Java Demo 的 LabelV2 协议（https://api.kuaidi100.com/label/order）；
 * 取消端点对齐官方 Java Demo 的 LabelCancel（http://poll.kuaidi100.com/eorderapi.do）。
 *
 * @author 书心软件
 */
@Component
@ConfigurationProperties(prefix = "suxin.trade.waybill")
@Data
@Validated
public class SalesElectronicWaybillProperties {

    /**
     * 电子面单下单/复打端点
     */
    private String orderUrl = "https://api.kuaidi100.com/label/order";

    /**
     * 电子面单取消端点
     */
    private String cancelUrl = "http://poll.kuaidi100.com/eorderapi.do";

    /**
     * 连接超时（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时（毫秒）
     */
    private Integer readTimeout = 10000;

}
