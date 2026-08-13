package vip.appap.suxin.module.hr.dal.dataobject.salary;

import lombok.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 薪资 DO
 *
 * @author admin
 */
@TableName("hr_salary")
@KeySequence("hr_salary_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联 hr_employee.id
     */
    private Long partnerId;
    /**
     * 所在部门（关联 system_dept.id，库字段 department）
     */
    @TableField("department")
    private Long dept;
    /**
     * 人员编号
     */
    private String employeeNo;
    /**
     * 人员类别
     */
    private String personnelCategory;
    /**
     * 年份
     */
    private Integer year;
    /**
     * 月份
     */
    private Integer month;
    /**
     * 岗位工资
     */
    private BigDecimal basicSalary;
    /**
     * 薪级工资
     */
    private BigDecimal salaryGrade;
    /**
     * 单位职补
     */
    private BigDecimal unitAllowance;
    /**
     * 岗位津贴
     */
    private BigDecimal postAllowance;
    /**
     * 独生子女
     */
    private BigDecimal onlyChildAllowance;
    /**
     * 回民补贴
     */
    private BigDecimal huiEthnicAllowance;
    /**
     * 计生兼职
     */
    private BigDecimal familyPlanningAllowance;
    /**
     * 福利费
     */
    private BigDecimal welfareFee;
    /**
     * 公务交通补贴
     */
    private BigDecimal officialTransportAllowance;
    /**
     * 提租补贴
     */
    private BigDecimal rentAllowance;
    /**
     * 反聘费
     */
    private BigDecimal rehireFee;
    /**
     * 补发工资
     */
    private BigDecimal backPay;
    /**
     * 其他工资
     */
    private BigDecimal otherWage;
    /**
     * 基础性绩效
     */
    private BigDecimal basicPerformance;
    /**
     * 绩效工资
     */
    private BigDecimal performanceSalary;
    /**
     * 应发合计
     */
    private BigDecimal grossSalaryTotal;
    /**
     * 社保基金
     */
    private BigDecimal socialSecurity;
    /**
     * 医保金
     */
    private BigDecimal medicalInsurance;
    /**
     * 职业年金
     */
    private BigDecimal occupationalAnnuity;
    /**
     * 失业金
     */
    private BigDecimal unemploymentInsurance;
    /**
     * 住房公积金
     */
    private BigDecimal housingFund;
    /**
     * 工会经费
     */
    private BigDecimal unionFee;
    /**
     * 房租费用
     */
    private BigDecimal rentFee;
    /**
     * 病事假
     */
    private BigDecimal sickLeaveDeduction;
    /**
     * 代扣所得税
     */
    private BigDecimal incomeTax;
    /**
     * 其他扣款
     */
    private BigDecimal otherDeduction;
    /**
     * 扣款合计
     */
    private BigDecimal totalDeduction;
    /**
     * 实发合计
     */
    private BigDecimal netSalaryTotal;
    /**
     * 所得基数
     */
    private BigDecimal taxBase;
    /**
     * 子女教育
     */
    private BigDecimal childEducation;
    /**
     * 继续教育
     */
    private BigDecimal continuingEducation;
    /**
     * 住房贷款利息
     */
    private BigDecimal housingLoanInterest;
    /**
     * 住房租金
     */
    private BigDecimal housingRent;
    /**
     * 老人赡养费
     */
    private BigDecimal elderlySupport;
    /**
     * 其他合法扣除
     */
    private BigDecimal otherLegalDeduction;

}
