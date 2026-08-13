package vip.appap.suxin.module.hr.controller.admin.outbound.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 外出记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OutboundRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联员工 partner.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30316")
    @ExcelProperty("员工ID")
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

    @Schema(description = "外出类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "rural_support")
    @ExcelProperty("外出类型")
    private String recordType;

    @Schema(description = "省份")
    @ExcelProperty("省份")
    private String province;

    @Schema(description = "市")
    @ExcelProperty("市")
    private String city;

    @Schema(description = "区/县")
    @ExcelProperty("区/县")
    private String county;

    @Schema(description = "进修/培训单位 或 下乡支援单位")
    @ExcelProperty("单位")
    private String organization;

    @Schema(description = "进修/培训名称")
    @ExcelProperty("名称")
    private String practiceName;

    @Schema(description = "开始日期")
    @ExcelProperty("开始日期")
    private LocalDateTime startDate;

    @Schema(description = "结束日期")
    @ExcelProperty("结束日期")
    private LocalDateTime endDate;

    @Schema(description = "外出天数")
    @ExcelProperty("天数")
    private Integer durationDays;

    @Schema(description = "服务年限（仅下乡支援）")
    @ExcelProperty("服务年限")
    private BigDecimal supportYears;

    @Schema(description = "继教学分")
    @ExcelProperty("继教学分")
    private BigDecimal continuingEducationCredit;

    @Schema(description = "是否计入汇总 1是 0否")
    @ExcelProperty("是否计入")
    private Integer effective;

    @Schema(description = "总结")
    @ExcelProperty("总结")
    private String summary;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
