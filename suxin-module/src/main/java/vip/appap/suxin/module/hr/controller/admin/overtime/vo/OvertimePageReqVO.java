package vip.appap.suxin.module.hr.controller.admin.overtime.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Schema(description = "管理后台 - 加班登记分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OvertimePageReqVO extends PageParam {

    @Schema(description = "关联员工", example = "10870")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "加班类型（字典 hr_overtime_type）", example = "big_night_shift")
    private String overtimeType;

    @Schema(description = "加班原因（字典 hr_overtime_reason）", example = "staff_shortage")
    private String overtimeReason;

    @Schema(description = "所属科室（system_dept.id）")
    private Long dept;

    @Schema(description = "实际值班科室（system_dept.id）")
    private Long workDeptId;

    @Schema(description = "归属日期范围")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] overtimeDate;

}
