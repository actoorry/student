package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - 学生新增/修改 Request VO")
@Data
public class StudentSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "学号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026001")
    @NotEmpty(message = "学号不能为空")
    private String studentNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "姓名不能为空")
    private String name;

    @Schema(description = "性别：1=男 2=女", example = "1")
    private Integer gender;

    @Schema(description = "班级", example = "高一（1）班")
    private String className;

    @Schema(description = "关联登录账号（system_users.id）", example = "100")
    private Long userId;

    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

}
