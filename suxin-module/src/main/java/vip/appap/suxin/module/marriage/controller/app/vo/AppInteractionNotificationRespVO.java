package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "App - App 互动通知 Response VO")
@Data
public class AppInteractionNotificationRespVO {

    @Schema(description = "通知编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long id;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "marriage")
    private String bizType;

    @Schema(description = "场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "FOLLOW")
    private String scene;

    @Schema(description = "接收用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "触发用户编号", example = "2")
    private Long actorId;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "关注提醒")
    private String title;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三关注了你")
    private String content;

    @Schema(description = "业务载荷")
    private Map<String, Object> payload;

    @Schema(description = "是否已读", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean readStatus;

    @Schema(description = "事件时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime eventTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
