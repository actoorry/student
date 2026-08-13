package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "App - Marriage follow response Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarriageFollowRespVO {

    @Schema(description = "Whether the follow relation exists", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean followed;

    @Schema(description = "Whether both users follow each other", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean mutual;

}
