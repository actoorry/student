package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 签到记录 Response VO")
@Data
public class AppPartnerSignInRecordRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer day;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "奖励经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer experience;

    @Schema(description = "签到时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
