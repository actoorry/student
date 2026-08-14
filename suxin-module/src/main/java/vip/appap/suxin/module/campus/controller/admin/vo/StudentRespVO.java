package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 学生 Response VO")
@Data
public class StudentRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026001")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "性别：1=男 2=女", example = "1")
    private Integer gender;

    @Schema(description = "班级", example = "高一（1）班")
    private String className;

    @Schema(description = "关联登录账号", example = "100")
    private Long userId;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    @Schema(description = "当前学期课程总分数", example = "356.5")
    private BigDecimal currentSemesterTotal;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
