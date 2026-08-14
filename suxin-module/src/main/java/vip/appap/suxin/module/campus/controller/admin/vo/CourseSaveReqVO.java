package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 课程新增/修改 Request VO")
@Data
public class CourseSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "课程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "C001")
    @NotEmpty(message = "课程编号不能为空")
    private String courseCode;

    @Schema(description = "课程名", requiredMode = Schema.RequiredMode.REQUIRED, example = "高等数学")
    @NotEmpty(message = "课程名不能为空")
    private String courseName;

    @Schema(description = "学分", example = "3.0")
    private BigDecimal credit;

    @Schema(description = "授课教师编号", example = "1")
    private Long teacherId;

}
