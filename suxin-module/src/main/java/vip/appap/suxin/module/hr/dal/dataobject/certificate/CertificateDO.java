package vip.appap.suxin.module.hr.dal.dataobject.certificate;

import lombok.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 人员证书 DO
 *
 * 卫健委专项督查台账核心表：记录员工各类证书（医师资格证/执业证/护士证/规培证等）
 * 的编号、发证机关、到期日、考核日、扫描件，用于督查筛选与到期提醒。
 *
 * @author admin
 */
@TableName("hr_certificate")
@KeySequence("hr_certificate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联 partner.id（员工）
     */
    private Long partnerId;
    /**
     * 员工工号（选员工时带出，便于不联表展示）
     */
    private String employeeNo;
    /**
     * 所在部门（关联 system_dept.id）
     */
    private Long dept;
    /**
     * 证书类型（字典 hr_certificate_type）
     */
    private String certificateType;
    /**
     * 证书名称（可覆盖字典默认名）
     */
    private String certificateName;
    /**
     * 证书编号
     */
    private String certificateNo;
    /**
     * 发证机关
     */
    private String issuingAuthority;
    /**
     * 发证日期
     */
    private LocalDateTime issueDate;
    /**
     * 到期日期（督查核心字段）
     */
    private LocalDateTime expireDate;
    /**
     * 上次考核日（手工维护）
     */
    private LocalDateTime lastAssessmentDate;
    /**
     * 下次考核日（手工维护）
     */
    private LocalDateTime nextAssessmentDate;
    /**
     * 证书扫描件 URL
     */
    private String attachment;
    /**
     * 备注
     */
    private String remark;

}
