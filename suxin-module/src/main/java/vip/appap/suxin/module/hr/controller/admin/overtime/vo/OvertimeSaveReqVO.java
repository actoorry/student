package vip.appap.suxin.module.hr.controller.admin.overtime.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 加班登记新增/修改 Request VO")
@Data
public class OvertimeSaveReqVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "10870")
    @NotNull(message = "关联员工不能为空")
    private Long partnerId;

    @Schema(description = "加班类型（字典 hr_overtime_type）", requiredMode = Schema.RequiredMode.REQUIRED, example = "big_night_shift")
    @NotBlank(message = "加班类型不能为空")
    private String overtimeType;

    @Schema(description = "加班原因（字典 hr_overtime_reason）", requiredMode = Schema.RequiredMode.REQUIRED, example = "staff_shortage")
    @NotBlank(message = "加班原因不能为空")
    private String overtimeReason;

    @Schema(description = "工作内容简述", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工作内容简述不能为空")
    private String workSummary;

    @Schema(description = "加班日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-06-27T00:00:00")
    @NotNull(message = "加班日期不能为空")
    private LocalDateTime overtimeDate;

    @Schema(description = "加班开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-06-27T22:00:00")
    @NotNull(message = "加班开始时间不能为空")
    private LocalDateTime startTime;

    @Schema(description = "加班结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-06-28T06:00:00")
    @NotNull(message = "加班结束时间不能为空")
    private LocalDateTime endTime;

    @Schema(description = "实际值班科室（关联 system_dept.id）")
    private Long workDeptId;

    @Schema(description = "值班地点")
    private String workLocation;

    @Schema(description = "节假日名称（仅 legal_holiday_duty 必填）")
    private String holidayName;

    @Schema(description = "备注")
    private String remark;

}
