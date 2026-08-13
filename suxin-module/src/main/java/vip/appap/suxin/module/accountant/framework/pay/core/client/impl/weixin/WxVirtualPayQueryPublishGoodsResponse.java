package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 微信虚拟支付查询批量发布道具任务响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxVirtualPayQueryPublishGoodsResponse extends WxVirtualPayCommonResponse {

    /**
     * 发布的道具列表
     */
    @JsonProperty("publish_item")
    private List<PublishItemResult> publishItem;

    /**
     * 任务状态：0-无任务在运行 1-任务运行中 2-发布失败或部分失败（任务已完成） 3-发布成功
     */
    @JsonProperty("status")
    private Integer status;

    @Data
    public static class PublishItemResult {

        /**
         * 道具id
         */
        @JsonProperty("id")
        private String id;

        /**
         * 0-上传中 1-id已经存在 2-发布成功 3-发布失败
         */
        @JsonProperty("publish_status")
        private Integer publishStatus;

        /**
         * 发布失败的原因
         */
        @JsonProperty("errmsg")
        private String errmsg;

    }

}
