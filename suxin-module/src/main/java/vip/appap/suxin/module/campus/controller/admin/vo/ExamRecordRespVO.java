package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 考试记录 Response VO")
@Data
public class ExamRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学生编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long studentId;

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "课程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long courseId;

    @Schema(description = "课程名称", example = "高等数学")
    private String courseName;

    @Schema(description = "学年，如 2026-2027", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-2027")
    private String schoolYear;

    @Schema(description = "学期：1=第一学期 2=第二学期", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer semester;

    @Schema(description = "分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "85.5")
    private BigDecimal score;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
