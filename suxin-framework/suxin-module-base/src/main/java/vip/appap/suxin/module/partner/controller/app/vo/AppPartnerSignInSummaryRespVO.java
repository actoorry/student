package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 签到摘要 Response VO")
@Data
public class AppPartnerSignInSummaryRespVO {

    @Schema(description = "今日是否已签到", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean signedToday;

    @Schema(description = "当前连续签到天数", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer currentDay;

    @Schema(description = "下次可获得奖励对应天数", example = "4")
    private Integer nextRewardDay;

    @Schema(description = "下次可获得积分", example = "10")
    private Integer nextRewardPoint;

    @Schema(description = "下次可获得经验", example = "5")
    private Integer nextRewardExperience;

    @Schema(description = "今日签到记录")
    private AppPartnerSignInRecordRespVO todayRecord;

    @Schema(description = "启用的签到奖励配置")
    private List<AppPartnerSignInConfigRespVO> configs;

}
