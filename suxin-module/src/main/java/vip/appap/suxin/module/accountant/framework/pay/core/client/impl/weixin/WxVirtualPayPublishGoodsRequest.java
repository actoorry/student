package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 微信虚拟支付批量发布道具请求。
 */
@Data
public class WxVirtualPayPublishGoodsRequest {

    /**
     * 发布的商品列表，一次仅支持发布一个道具
     */
    @JsonProperty("publish_item")
    private List<WxVirtualPayPublishItem> publishItem;

    /**
     * 0-正式环境 1-沙箱环境
     */
    @JsonProperty("env")
    private Integer env;

}
