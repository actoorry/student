package vip.appap.suxin.module.hr.controller.admin.overtime.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 加班登记 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OvertimeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联员工 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "10870")
    @ExcelProperty("关联员工 id")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    @ExcelProperty("姓名")
    private String name;

    @Schema(description = "员工工号")
    @ExcelProperty("工号")
    private String employeeNo;

    @Schema(description = "所属科室 id")
    private Long dept;

    @Schema(description = "所属科室")
    @ExcelProperty("所属科室")
    private String deptName;

    @Schema(description = "加班类型（字典 hr_overtime_type）", example = "big_night_shift")
    @ExcelProperty("加班类型")
    private String overtimeType;

    @Schema(description = "加班原因（字典 hr_overtime_reason）", example = "staff_shortage")
    @ExcelProperty("加班原因")
    private String overtimeReason;

    @Schema(description = "工作内容简述")
    @ExcelProperty("工作内容简述")
    private String workSummary;

    @Schema(description = "加班日期")
    @ExcelProperty("加班日期")
    private LocalDateTime overtimeDate;

    @Schema(description = "加班开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @Schema(description = "加班结束时间")
    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @Schema(description = "加班时长（小时）")
    @ExcelProperty("时长(小时)")
    private BigDecimal durationHours;

    @Schema(description = "实际值班科室 id")
    private Long workDeptId;

    @Schema(description = "实际值班科室")
    @ExcelProperty("值班科室")
    private String workDeptName;

    @Schema(description = "值班地点")
    @ExcelProperty("值班地点")
    private String workLocation;

    @Schema(description = "节假日名称")
    @ExcelProperty("节假日名称")
    private String holidayName;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
