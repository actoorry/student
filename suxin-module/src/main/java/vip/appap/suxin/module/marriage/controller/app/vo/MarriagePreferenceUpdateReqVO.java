package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 更新择偶条件 Request VO")
@Data
public class MarriagePreferenceUpdateReqVO {

    @Schema(description = "期望婚姻状态", example = "1")
    private Integer mateMaritalStatus;

    @Schema(description = "期望最小身高(cm)", example = "160")
    private Integer mateMinHeightCm;

    @Schema(description = "期望最大身高(cm)", example = "175")
    private Integer mateMaxHeightCm;

    @Schema(description = "期望最小体重(kg)", example = "45")
    private Integer mateMinWeightKg;

    @Schema(description = "期望最大体重(kg)", example = "65")
    private Integer mateMaxWeightKg;

    @Schema(description = "期望最低学历", example = "3")
    private Integer mateMinEducation;

    @Schema(description = "期望现居地ID", example = "1024")
    private Long mateLiveAreaId;

    @Schema(description = "期望最低月收入水平", example = "5")
    private Integer mateMinIncomeLevel;

    @Schema(description = "期望房产状况", example = "1")
    private Integer mateHouseStatus;

    @Schema(description = "期望车辆状况", example = "1")
    private Integer mateCarStatus;

    @Schema(description = "期望职业", example = "不限")
    private String mateJobTitle;

    @Schema(description = "其他要求", example = "性格开朗")
    private String mateRemark;

}
