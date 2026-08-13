package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会员经验记录 Response VO")
@Data
public class PartnerExperienceRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "业务编号", example = "1")
    private String bizId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

    @Schema(description = "标题", example = "签到")
    private String title;

    @Schema(description = "描述", example = "签到奖励")
    private String description;

    @Schema(description = "经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer experience;

    @Schema(description = "变更后的经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalExperience;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
