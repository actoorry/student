package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "App - 更新我的资料 Request VO")
@Data
public class MarriageProfileUpdateReqVO {

    @Schema(description = "昵称", example = "小明")
    private String nickname;

    @Schema(description = "性别：1-男，2-女", example = "1")
    private Integer sex;

    @Schema(description = "生日，格式 YYYY-MM-DD", example = "1995-01-01")
    private String birthday;

    @Schema(description = "籍贯地区ID", example = "440100")
    private Long areaId;

    @Schema(description = "身高(cm)", example = "175")
    private Integer heightCm;

    @Schema(description = "体重(kg)", example = "70")
    private Integer weightKg;

    @Schema(description = "学历", example = "3")
    private Integer education;

    @Schema(description = "月收入水平", example = "5")
    private Integer incomeLevel;

    @Schema(description = "现居地ID", example = "1024")
    private Long liveAreaId;

    @Schema(description = "婚姻状况", example = "1")
    private Integer maritalStatus;

    @Schema(description = "房产状况", example = "1")
    private Integer houseStatus;

    @Schema(description = "车辆状况", example = "1")
    private Integer carStatus;

    @Schema(description = "职业", example = "软件工程师")
    private String jobTitle;

    @Schema(description = "自我介绍", example = "热爱生活")
    private String bio;

}
