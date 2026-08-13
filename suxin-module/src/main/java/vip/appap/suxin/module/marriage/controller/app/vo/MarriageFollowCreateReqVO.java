package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "App - Marriage follow create Request VO")
@Data
public class MarriageFollowCreateReqVO {

    @Schema(description = "Target partner id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "Target partner id cannot be empty")
    private Long relPartnerId;

}
