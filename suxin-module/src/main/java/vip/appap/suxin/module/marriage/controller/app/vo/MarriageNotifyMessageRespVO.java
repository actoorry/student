package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "App - Marriage notify message Response VO")
@Data
public class MarriageNotifyMessageRespVO {

    @Schema(description = "Message id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Template code", example = "marriage_follow_1")
    private String templateCode;

    @Schema(description = "Template nickname", example = "System")
    private String templateNickname;

    @Schema(description = "Message content", example = "Zhang San followed you")
    private String templateContent;

    @Schema(description = "Template params")
    private Map<String, Object> templateParams;

    @Schema(description = "Read status", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean readStatus;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

}
