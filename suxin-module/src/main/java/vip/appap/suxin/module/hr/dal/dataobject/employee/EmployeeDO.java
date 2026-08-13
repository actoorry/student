package vip.appap.suxin.module.hr.dal.dataobject.employee;

import lombok.*;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 员工 DO
 *
 * @author admin
 */
@TableName("hr_employee")
@KeySequence("hr_employee_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联 partner.id
     */
    private Long partnerId;
    /**
     * 员工工号
     */
    private String employeeNo;
    /**
     * 员工手机号（人事专用，区别于 partner.mobile）
     */
    private String employeeMobile;
    /**
     * 所在部门（关联 system_dept.id）
     */
    private Long dept;
    /**
     * 岗位编号（关联 system_post.id）
     */
    private Long postId;
    /**
     * 人员类别
     */
    private String personnelCategory;
    /**
     * 职称
     */
    private String professionalTitle;
    /**
     * 政治面貌
     */
    private String politicalStatus;
    /**
     * 入党时间
     */
    private LocalDate partyJoinDate;
    /**
     * 参加工作时间
     */
    private LocalDate careerStartDate;
    /**
     * 进入单位时间
     */
    private LocalDate hireDate;
    /**
     * 岗位
     */
    private String position;
    /**
     * 职务
     */
    private String duty;
    /**
     * 任职时间
     */
    private LocalDate appointmentDate;
    /**
     * 最高学历
     */
    private String highestEducation;
    /**
     * 全日制学历
     */
    private String fullTimeEducation;
    /**
     * 是否在编
     */
    private String establishmentStatus;
    /**
     * 人员身份
     */
    private String personnelIdentity;
    /**
     * 人员来源
     */
    private String recruitmentSource;
    /**
     * 进入形式
     */
    private String entryMode;
    /**
     * 在职状态
     */
    private String employmentStatus;
    /**
     * 民族
     */
    private String ethnicity;
    /**
     * 籍贯-省
     */
    private String nativeProvince;
    /**
     * 籍贯-市
     */
    private String nativeCity;
    /**
     * 银行卡卡号
     */
    private String bankCard;
    /**
     * 家庭信息
     */
    private String homeInformation;
    /**
     * 毕业院校及专业
     */
    private String schoolMajor;
    /**
     * 职称医护（图表辅助）
     */
    private String professionalCategory;
    /**
     * 姓名简写
     */
    private String nameAbbreviation;


}
