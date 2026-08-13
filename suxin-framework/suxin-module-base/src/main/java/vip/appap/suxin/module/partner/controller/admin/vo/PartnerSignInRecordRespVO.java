package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会员签到记录 Response VO")
@Data
public class PartnerSignInRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer day;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "奖励经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer experience;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
