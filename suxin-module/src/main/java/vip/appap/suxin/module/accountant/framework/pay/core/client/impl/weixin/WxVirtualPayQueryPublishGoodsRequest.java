package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信虚拟支付查询批量发布道具任务请求。
 */
@Data
public class WxVirtualPayQueryPublishGoodsRequest {

    /**
     * 0-正式环境 1-沙箱环境
     */
    @JsonProperty("env")
    private Integer env;

}
