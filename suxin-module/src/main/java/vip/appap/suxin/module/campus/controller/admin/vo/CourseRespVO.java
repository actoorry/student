package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 课程 Response VO")
@Data
public class CourseRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "课程编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "C001")
    private String courseCode;

    @Schema(description = "课程名", requiredMode = Schema.RequiredMode.REQUIRED, example = "高等数学")
    private String courseName;

    @Schema(description = "学分", example = "3.0")
    private BigDecimal credit;

    @Schema(description = "授课教师编号", example = "1")
    private Long teacherId;

    @Schema(description = "授课教师姓名", example = "李四")
    private String teacherName;

    @Schema(description = "及格总人数", example = "35")
    private Long passCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
