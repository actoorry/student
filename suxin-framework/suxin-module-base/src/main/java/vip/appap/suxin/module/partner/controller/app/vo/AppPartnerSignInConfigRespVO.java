package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 签到配置 Response VO")
@Data
public class AppPartnerSignInConfigRespVO {

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer id;

    @Schema(description = "签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer day;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "奖励经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer experience;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

}
