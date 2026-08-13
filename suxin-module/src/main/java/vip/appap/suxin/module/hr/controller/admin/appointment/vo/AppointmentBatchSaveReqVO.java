package vip.appap.suxin.module.hr.controller.admin.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 批量岗位聘任 Request VO")
@Data
public class AppointmentBatchSaveReqVO {

    @Schema(description = "员工 partner.id 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "员工列表不能为空")
    private List<Long> partnerIds;

    @Schema(description = "岗位类别", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "岗位类别不能为空")
    private String postCategory;

    @Schema(description = "岗位等级", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "聘期年数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "聘期年数不能为空")
    private Integer termYears;

    @Schema(description = "聘任文件附件")
    private String appointmentDoc;

    @Schema(description = "备注")
    private String remark;

}
