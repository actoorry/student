package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 考试记录新增/修改 Request VO")
@Data
public class ExamRecordSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学生编号不能为空")
    private Long studentId;

    @Schema(description = "课程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "课程编号不能为空")
    private Long courseId;

    @Schema(description = "学年，如 2026-2027", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027")
    @NotEmpty(message = "学年不能为空")
    private String schoolYear;

    @Schema(description = "学期：1=第一学期 2=第二学期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "学期不能为空")
    private Integer semester;

    @Schema(description = "分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "85.5")
    @NotNull(message = "分数不能为空")
    private BigDecimal score;

}
