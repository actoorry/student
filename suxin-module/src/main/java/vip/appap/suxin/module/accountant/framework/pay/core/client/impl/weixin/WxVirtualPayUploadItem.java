package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信虚拟支付上传道具项。
 */
@Data
public class WxVirtualPayUploadItem {

    /**
     * 道具id，长度(0,20]，字符只允许使用字母、数字、'_'、'-'，中文算一个字符
     */
    @JsonProperty("id")
    private String id;

    /**
     * 道具名称，长度(0，20]
     */
    @JsonProperty("name")
    private String name;

    /**
     * 道具单价，单位分，需要大于0
     */
    @JsonProperty("price")
    private Integer price;

    /**
     * 道具备注，长度(0,1024]
     */
    @JsonProperty("remark")
    private String remark;

    /**
     * 道具图片的url地址，当前仅支持jpg,png等格式
     */
    @JsonProperty("item_url")
    private String itemUrl;

}
