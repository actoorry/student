package vip.appap.suxin.module.campus.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 APP - 我的成绩 Response VO")
@Data
public class AppExamRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

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
