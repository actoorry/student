package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 微信虚拟支付批量上传道具请求。
 */
@Data
public class WxVirtualPayUploadGoodsRequest {

    /**
     * 上传的商品列表，一次仅支持上传一个道具
     */
    @JsonProperty("upload_item")
    private List<WxVirtualPayUploadItem> uploadItem;

    /**
     * 0-正式环境 1-沙箱环境
     */
    @JsonProperty("env")
    private Integer env;

}
