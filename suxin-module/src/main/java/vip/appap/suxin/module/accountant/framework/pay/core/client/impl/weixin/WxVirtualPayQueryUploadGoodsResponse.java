package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 微信虚拟支付查询批量上传道具任务响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxVirtualPayQueryUploadGoodsResponse extends WxVirtualPayCommonResponse {

    /**
     * 上传的道具列表
     */
    @JsonProperty("upload_item")
    private List<UploadItemResult> uploadItem;

    /**
     * 任务状态：0-无任务在运行 1-任务运行中 2-上传失败或部分失败（任务已完成） 3-上传成功
     */
    @JsonProperty("status")
    private Integer status;

    @Data
    public static class UploadItemResult {

        /**
         * 道具id
         */
        @JsonProperty("id")
        private String id;

        /**
         * 道具名称
         */
        @JsonProperty("name")
        private String name;

        /**
         * 道具单价，单位分
         */
        @JsonProperty("price")
        private Integer price;

        /**
         * 道具备注
         */
        @JsonProperty("remark")
        private String remark;

        /**
         * 道具图片的url地址（微信转存后）
         */
        @JsonProperty("item_url")
        private String itemUrl;

        /**
         * 0-上传中 1-id已经存在 2-上传成功 3-上传失败
         */
        @JsonProperty("upload_status")
        private Integer uploadStatus;

        /**
         * 上传失败的原因
         */
        @JsonProperty("errmsg")
        private String errmsg;

    }

}
