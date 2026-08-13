package vip.appap.suxin.module.marriage.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "App - App 互动通知分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppInteractionNotificationPageReqVO extends PageParam {

    @Schema(description = "场景", example = "FOLLOW")
    private String scene;

    @Schema(description = "已读状态", example = "false")
    private Boolean readStatus;

}
