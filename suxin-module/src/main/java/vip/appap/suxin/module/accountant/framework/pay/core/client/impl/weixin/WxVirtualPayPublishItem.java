package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信虚拟支付发布道具项。
 */
@Data
public class WxVirtualPayPublishItem {

    /**
     * 道具id，添加到开发环境时传的道具id
     */
    @JsonProperty("id")
    private String id;

}
