package vip.appap.suxin.module.hr.controller.admin.resume.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 履历新增/修改 Request VO")
@Data
public class ResumeSaveReqVO {

    @Schema(description = "主键", example = "10675")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "16087")
    @NotNull(message = "关联员工不能为空")
    private Long partnerId;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "所在部门")
    private String dept;

    @Schema(description = "个人简历")
    private String resumeContent;

    @Schema(description = "奖惩情况")
    private String awardsPunishments;

    @Schema(description = "证书")
    private String certificates;

    @Schema(description = "论文")
    private String papers;

    @Schema(description = "年度考核情况")
    private String annualReview;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "市")
    private String city;

    @Schema(description = "县")
    private String county;

}
