package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "管理后台 - 教师新增/修改 Request VO")
@Data
public class TeacherSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "T001")
    @NotEmpty(message = "工号不能为空")
    private String teacherNo;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotEmpty(message = "姓名不能为空")
    private String name;

    @Schema(description = "性别：1=男 2=女", example = "2")
    private Integer gender;

    @Schema(description = "职称", example = "教授")
    private String title;

    @Schema(description = "关联登录账号（system_users.id）", example = "101")
    private Long userId;

    @Schema(description = "手机号", example = "13900139000")
    private String mobile;

}
