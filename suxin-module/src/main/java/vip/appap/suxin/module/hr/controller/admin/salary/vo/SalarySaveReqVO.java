package vip.appap.suxin.module.hr.controller.admin.salary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 薪资新增/修改 Request VO")
@Data
public class SalarySaveReqVO {

    @Schema(description = "主键", example = "29256")
    private Long id;

    @Schema(description = "关联员工 id", example = "7492")
    private Long partnerId;

    @Schema(description = "所在部门（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "人员编号")
    private String employeeNo;

    @Schema(description = "人员类别")
    private String personnelCategory;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "岗位工资")
    private BigDecimal basicSalary;

    @Schema(description = "薪级工资")
    private BigDecimal salaryGrade;

    @Schema(description = "单位职补")
    private BigDecimal unitAllowance;

    @Schema(description = "岗位津贴")
    private BigDecimal postAllowance;

    @Schema(description = "独生子女")
    private BigDecimal onlyChildAllowance;

    @Schema(description = "回民补贴")
    private BigDecimal huiEthnicAllowance;

    @Schema(description = "计生兼职")
    private BigDecimal familyPlanningAllowance;

    @Schema(description = "福利费")
    private BigDecimal welfareFee;

    @Schema(description = "公务交通补贴")
    private BigDecimal officialTransportAllowance;

    @Schema(description = "提租补贴")
    private BigDecimal rentAllowance;

    @Schema(description = "反聘费")
    private BigDecimal rehireFee;

    @Schema(description = "补发工资")
    private BigDecimal backPay;

    @Schema(description = "其他工资")
    private BigDecimal otherWage;

    @Schema(description = "基础性绩效")
    private BigDecimal basicPerformance;

    @Schema(description = "绩效工资")
    private BigDecimal performanceSalary;

    @Schema(description = "应发合计")
    private BigDecimal grossSalaryTotal;

    @Schema(description = "社保基金")
    private BigDecimal socialSecurity;

    @Schema(description = "医保金")
    private BigDecimal medicalInsurance;

    @Schema(description = "职业年金")
    private BigDecimal occupationalAnnuity;

    @Schema(description = "失业金")
    private BigDecimal unemploymentInsurance;

    @Schema(description = "住房公积金")
    private BigDecimal housingFund;

    @Schema(description = "工会经费")
    private BigDecimal unionFee;

    @Schema(description = "房租费用")
    private BigDecimal rentFee;

    @Schema(description = "病事假")
    private BigDecimal sickLeaveDeduction;

    @Schema(description = "代扣所得税")
    private BigDecimal incomeTax;

    @Schema(description = "其他扣款")
    private BigDecimal otherDeduction;

    @Schema(description = "扣款合计")
    private BigDecimal totalDeduction;

    @Schema(description = "实发合计")
    private BigDecimal netSalaryTotal;

    @Schema(description = "所得基数")
    private BigDecimal taxBase;

    @Schema(description = "子女教育")
    private BigDecimal childEducation;

    @Schema(description = "继续教育")
    private BigDecimal continuingEducation;

    @Schema(description = "住房贷款利息")
    private BigDecimal housingLoanInterest;

    @Schema(description = "住房租金")
    private BigDecimal housingRent;

    @Schema(description = "老人赡养费")
    private BigDecimal elderlySupport;

    @Schema(description = "其他合法扣除")
    private BigDecimal otherLegalDeduction;

}
