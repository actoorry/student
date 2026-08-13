package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 婚姻认证 Response VO")
@Data
public class PartnerMarriageCheckRespVO {

    @Schema(description = "是否成功拿到认证结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean success;

    @Schema(description = "婚姻状态", example = "2")
    private String state;

    @Schema(description = "婚姻状态文案", example = "未婚")
    private String stateLabel;

    @Schema(description = "结果说明", example = "婚姻认证结果：未婚")
    private String reason;

    @Schema(description = "是否复用历史认证结果")
    private Boolean reused;

}
