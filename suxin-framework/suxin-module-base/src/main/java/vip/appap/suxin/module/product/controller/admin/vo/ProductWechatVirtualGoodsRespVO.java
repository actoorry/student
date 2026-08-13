package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 微信虚拟支付道具 Response VO")
@Data
public class ProductWechatVirtualGoodsRespVO {

    @Schema(description = "SKU 编号", example = "1001")
    private Long skuId;

    @Schema(description = "SPU 编号", example = "100")
    private Long spuId;

    @Schema(description = "SKU 规格名称", example = "默认")
    private String skuName;

    @Schema(description = "微信 ProductId", example = "product_1001")
    private String wechatVirtualProductId;

    @Schema(description = "上传任务编号")
    private String wechatVirtualUploadTaskId;

    @Schema(description = "上传状态")
    private Integer wechatVirtualUploadStatus;

    @Schema(description = "发布任务编号")
    private String wechatVirtualPublishTaskId;

    @Schema(description = "发布状态")
    private Integer wechatVirtualPublishStatus;

    @Schema(description = "审核状态")
    private Integer wechatVirtualReviewStatus;

    @Schema(description = "审核失败原因")
    private String wechatVirtualReviewFailReason;

    @Schema(description = "最后同步时间")
    private LocalDateTime wechatVirtualLastSyncTime;

    // ========== 本次操作结果（仅 sync/publish/refresh 返回） ==========

    @Schema(description = "本次动作：sync / publish / refresh", example = "sync")
    private String action;

    @Schema(description = "本次动作是否成功", example = "true")
    private Boolean actionSuccess;

    @Schema(description = "本次动作结果描述", example = "已提交微信处理")
    private String actionMessage;

}
