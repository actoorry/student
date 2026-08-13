package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 实名认证 Response VO")
@Data
public class PartnerNameCheckRespVO {

    @Schema(description = "是否认证通过", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean passed;

    @Schema(description = "认证结果说明", example = "实名认证通过")
    private String reason;

    @Schema(description = "是否复用历史认证结果")
    private Boolean reused;

}
