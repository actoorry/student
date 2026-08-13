package vip.appap.suxin.module.hr.controller.admin.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 岗位聘任新增/修改 Request VO")
@Data
public class AppointmentSaveReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联员工 partner.id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "员工不能为空")
    private Long partnerId;

    @Schema(description = "岗位类别 hr_post_category", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "岗位类别不能为空")
    private String postCategory;

    @Schema(description = "岗位等级 hr_post_level", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "岗位等级不能为空")
    private String postLevel;

    @Schema(description = "岗位编号 system_post.id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "聘任岗位不能为空")
    private Long postId;

    @Schema(description = "聘任岗位名称（快照，后端按系统岗位名称写入）")
    private String postName;

    @Schema(description = "聘任起始日", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "聘任起始日不能为空")
    private LocalDateTime startDate;

    @Schema(description = "聘任到期日")
    private LocalDateTime endDate;

    @Schema(description = "聘期年数 3~5")
    private Integer termYears;

    @Schema(description = "聘任文件附件")
    private String appointmentDoc;

    @Schema(description = "状态 hr_appointment_status")
    private String status;

    @Schema(description = "是否当前有效聘任 1是 0否")
    private Integer isCurrent;

    @Schema(description = "备注")
    private String remark;

}
