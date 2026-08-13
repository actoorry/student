package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "App - App 互动通知场景未读数 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppInteractionNotificationUnreadCountRespVO {

    @Schema(description = "场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "FOLLOW")
    private String scene;

    @Schema(description = "未读数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long unreadCount;

}
