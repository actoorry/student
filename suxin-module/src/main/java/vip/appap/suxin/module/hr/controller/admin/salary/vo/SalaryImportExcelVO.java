package vip.appap.suxin.module.hr.controller.admin.salary.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 薪资导入 Excel VO（客户 39 列格式）
 *
 * <p>年月不在 Excel 列中，由文件名（如「2025年12月份工资数据.xls」）解析。
 * 列顺序与客户工资表一致，FastExcel 按表头名称匹配，顺序不影响读取。</p>
 */
@Schema(description = "管理后台 - 薪资导入 Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaryImportExcelVO {

    @ExcelProperty("人员编号")
    private String employeeNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("部门")
    private String department;

    @ExcelProperty("人员类别")
    private String personnelCategory;

    @ExcelProperty("岗位工资")
    private BigDecimal basicSalary;

    @ExcelProperty("薪级工资")
    private BigDecimal salaryGrade;

    @ExcelProperty("单位职补")
    private BigDecimal unitAllowance;

    @ExcelProperty("独生子女")
    private BigDecimal onlyChildAllowance;

    @ExcelProperty("回民补贴")
    private BigDecimal huiEthnicAllowance;

    @ExcelProperty("岗位津贴")
    private BigDecimal postAllowance;

    @ExcelProperty("计生兼职")
    private BigDecimal familyPlanningAllowance;

    @ExcelProperty("福利费")
    private BigDecimal welfareFee;

    @ExcelProperty("公务交通补贴")
    private BigDecimal officialTransportAllowance;

    @ExcelProperty("反聘费")
    private BigDecimal rehireFee;

    @ExcelProperty("补发工资")
    private BigDecimal backPay;

    @ExcelProperty("其他工资")
    private BigDecimal otherWage;

    @ExcelProperty("提租补贴")
    private BigDecimal rentAllowance;

    @ExcelProperty("基础性绩效")
    private BigDecimal basicPerformance;

    @ExcelProperty("绩效工资")
    private BigDecimal performanceSalary;

    @ExcelProperty("应发合计")
    private BigDecimal grossSalaryTotal;

    @ExcelProperty("社保基金")
    private BigDecimal socialSecurity;

    @ExcelProperty("医保金")
    private BigDecimal medicalInsurance;

    @ExcelProperty("职业年金")
    private BigDecimal occupationalAnnuity;

    @ExcelProperty("房租费用")
    private BigDecimal rentFee;

    @ExcelProperty("病事假")
    private BigDecimal sickLeaveDeduction;

    @ExcelProperty("代扣所得税")
    private BigDecimal incomeTax;

    @ExcelProperty("其他扣款")
    private BigDecimal otherDeduction;

    @ExcelProperty("失业金")
    private BigDecimal unemploymentInsurance;

    @ExcelProperty("住房公积金")
    private BigDecimal housingFund;

    @ExcelProperty("工会经费")
    private BigDecimal unionFee;

    @ExcelProperty("扣款合计")
    private BigDecimal totalDeduction;

    @ExcelProperty("实发合计")
    private BigDecimal netSalaryTotal;

    @ExcelProperty("所得基数")
    private BigDecimal taxBase;

    @ExcelProperty("子女教育")
    private BigDecimal childEducation;

    @ExcelProperty("继续教育")
    private BigDecimal continuingEducation;

    @ExcelProperty("住房贷款利息")
    private BigDecimal housingLoanInterest;

    @ExcelProperty("住房租金")
    private BigDecimal housingRent;

    @ExcelProperty("老人赡养费")
    private BigDecimal elderlySupport;

    @ExcelProperty("其他合法扣除")
    private BigDecimal otherLegalDeduction;

}
