package vip.appap.suxin.module.hr.controller.admin.salary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 薪资 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SalaryRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29256")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联员工 id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7492")
    @ExcelProperty("关联员工 id")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    @ExcelProperty("姓名")
    private String name;

    @Schema(description = "部门名称")
    @ExcelProperty("部门")
    private String deptName;

    @Schema(description = "所在部门（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "人员编号")
    @ExcelProperty("人员编号")
    private String employeeNo;

    @Schema(description = "人员类别")
    @ExcelProperty("人员类别")
    private String personnelCategory;

    @Schema(description = "年份")
    @ExcelProperty("年份")
    private Integer year;

    @Schema(description = "月份")
    @ExcelProperty("月份")
    private Integer month;

    @Schema(description = "岗位工资")
    @ExcelProperty("岗位工资")
    private BigDecimal basicSalary;

    @Schema(description = "薪级工资")
    @ExcelProperty("薪级工资")
    private BigDecimal salaryGrade;

    @Schema(description = "单位职补")
    @ExcelProperty("单位职补")
    private BigDecimal unitAllowance;

    @Schema(description = "岗位津贴")
    @ExcelProperty("岗位津贴")
    private BigDecimal postAllowance;

    @Schema(description = "独生子女")
    @ExcelProperty("独生子女")
    private BigDecimal onlyChildAllowance;

    @Schema(description = "回民补贴")
    @ExcelProperty("回民补贴")
    private BigDecimal huiEthnicAllowance;

    @Schema(description = "计生兼职")
    @ExcelProperty("计生兼职")
    private BigDecimal familyPlanningAllowance;

    @Schema(description = "福利费")
    @ExcelProperty("福利费")
    private BigDecimal welfareFee;

    @Schema(description = "公务交通补贴")
    @ExcelProperty("公务交通补贴")
    private BigDecimal officialTransportAllowance;

    @Schema(description = "提租补贴")
    @ExcelProperty("提租补贴")
    private BigDecimal rentAllowance;

    @Schema(description = "反聘费")
    @ExcelProperty("反聘费")
    private BigDecimal rehireFee;

    @Schema(description = "补发工资")
    @ExcelProperty("补发工资")
    private BigDecimal backPay;

    @Schema(description = "其他工资")
    @ExcelProperty("其他工资")
    private BigDecimal otherWage;

    @Schema(description = "基础性绩效")
    @ExcelProperty("基础性绩效")
    private BigDecimal basicPerformance;

    @Schema(description = "绩效工资")
    @ExcelProperty("绩效工资")
    private BigDecimal performanceSalary;

    @Schema(description = "应发合计")
    @ExcelProperty("应发合计")
    private BigDecimal grossSalaryTotal;

    @Schema(description = "社保基金")
    @ExcelProperty("社保基金")
    private BigDecimal socialSecurity;

    @Schema(description = "医保金")
    @ExcelProperty("医保金")
    private BigDecimal medicalInsurance;

    @Schema(description = "职业年金")
    @ExcelProperty("职业年金")
    private BigDecimal occupationalAnnuity;

    @Schema(description = "失业金")
    @ExcelProperty("失业金")
    private BigDecimal unemploymentInsurance;

    @Schema(description = "住房公积金")
    @ExcelProperty("住房公积金")
    private BigDecimal housingFund;

    @Schema(description = "工会经费")
    @ExcelProperty("工会经费")
    private BigDecimal unionFee;

    @Schema(description = "房租费用")
    @ExcelProperty("房租费用")
    private BigDecimal rentFee;

    @Schema(description = "病事假")
    @ExcelProperty("病事假")
    private BigDecimal sickLeaveDeduction;

    @Schema(description = "代扣所得税")
    @ExcelProperty("代扣所得税")
    private BigDecimal incomeTax;

    @Schema(description = "其他扣款")
    @ExcelProperty("其他扣款")
    private BigDecimal otherDeduction;

    @Schema(description = "扣款合计")
    @ExcelProperty("扣款合计")
    private BigDecimal totalDeduction;

    @Schema(description = "实发合计")
    @ExcelProperty("实发合计")
    private BigDecimal netSalaryTotal;

    @Schema(description = "所得基数")
    @ExcelProperty("所得基数")
    private BigDecimal taxBase;

    @Schema(description = "子女教育")
    @ExcelProperty("子女教育")
    private BigDecimal childEducation;

    @Schema(description = "继续教育")
    @ExcelProperty("继续教育")
    private BigDecimal continuingEducation;

    @Schema(description = "住房贷款利息")
    @ExcelProperty("住房贷款利息")
    private BigDecimal housingLoanInterest;

    @Schema(description = "住房租金")
    @ExcelProperty("住房租金")
    private BigDecimal housingRent;

    @Schema(description = "老人赡养费")
    @ExcelProperty("老人赡养费")
    private BigDecimal elderlySupport;

    @Schema(description = "其他合法扣除")
    @ExcelProperty("其他合法扣除")
    private BigDecimal otherLegalDeduction;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
