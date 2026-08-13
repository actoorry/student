package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 我的活动 Response VO")
@Data
public class AppSalesOrderActivityRespVO {

    @Schema(description = "订单项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long orderItemId;

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long orderId;

    @Schema(description = "订单状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer orderStatus;

    @Schema(description = "支付时间", example = "2026-06-24T10:00:00")
    private LocalDateTime payTime;

    @Schema(description = "活动 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "95")
    private Long spuId;

    @Schema(description = "活动 SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "901")
    private Long skuId;

    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "周末联谊会")
    private String spuName;

    @Schema(description = "活动图片", requiredMode = Schema.RequiredMode.REQUIRED)
    private String picUrl;

    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;

    @Schema(description = "订单项支付金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "6800")
    private Integer payPrice;
}
