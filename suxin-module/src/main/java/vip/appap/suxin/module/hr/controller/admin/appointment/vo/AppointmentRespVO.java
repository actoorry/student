package vip.appap.suxin.module.hr.controller.admin.appointment.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import vip.appap.suxin.module.hr.convert.appointment.AppointmentStatusConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 岗位聘任 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppointmentRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联员工 partner.id")
    private Long partnerId;

    @Schema(description = "员工姓名")
    @ExcelProperty("员工姓名")
    private String partnerName;

    @Schema(description = "员工工号")
    @ExcelProperty("员工工号")
    private String employeeNo;

    @Schema(description = "部门名称")
    @ExcelProperty("部门")
    private String deptName;

    @Schema(description = "部门 id")
    private Long dept;

    @Schema(description = "岗位类别")
    @ExcelProperty("岗位类别")
    private String postCategory;

    @Schema(description = "岗位等级")
    @ExcelProperty("岗位等级")
    private String postLevel;

    @Schema(description = "岗位编号 system_post.id")
    private Long postId;

    @Schema(description = "聘任岗位名称")
    @ExcelProperty("聘任岗位")
    private String postName;

    @Schema(description = "聘任起始日")
    @ExcelProperty("起始日")
    private LocalDateTime startDate;

    @Schema(description = "聘任到期日")
    @ExcelProperty("到期日")
    private LocalDateTime endDate;

    @Schema(description = "聘期年数")
    @ExcelProperty("聘期(年)")
    private Integer termYears;

    @Schema(description = "聘任文件")
    private String appointmentDoc;

    @Schema(description = "状态")
    @ExcelProperty(value = "状态", converter = AppointmentStatusConvert.class)
    private String status;

    @Schema(description = "是否当前有效聘任")
    @ExcelProperty("当前聘任")
    private Integer isCurrent;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /** 距到期天数（SQL 动态计算，仅列表/Job 用） */
    private Integer daysToExpire;

}
