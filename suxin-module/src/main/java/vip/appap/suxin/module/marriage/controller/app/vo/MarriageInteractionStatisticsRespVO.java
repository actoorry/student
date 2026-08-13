package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema(description = "App - Marriage interaction statistics Response VO")
@Data
@Accessors(chain = true)
public class MarriageInteractionStatisticsRespVO {

    @Schema(description = "Users who viewed me count", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long viewMeCount;

    @Schema(description = "Users I viewed count", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long myViewCount;

    @Schema(description = "Users who follow me count", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    private Long followMeCount;

    @Schema(description = "Users I follow count", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Long myFollowCount;

}
