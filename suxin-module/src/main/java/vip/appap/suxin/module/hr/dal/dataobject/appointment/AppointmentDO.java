package vip.appap.suxin.module.hr.dal.dataobject.appointment;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * HR 岗位聘任 DO
 */
@TableName("hr_appointment")
@KeySequence("hr_appointment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDO extends BaseDO {

    @TableId
    private Long id;
    /** 关联 partner.id */
    private Long partnerId;
    /** 员工工号（冗余） */
    private String employeeNo;
    /** 部门 system_dept.id */
    private Long dept;
    /** 岗位类别 hr_post_category */
    private String postCategory;
    /** 岗位等级 hr_post_level */
    private String postLevel;
    /** 岗位编号，关联 system_post.id */
    private Long postId;
    /** 聘任岗位名称（快照，由后端按系统岗位名称写入） */
    private String postName;
    /** 聘任起始日 */
    private LocalDateTime startDate;
    /** 聘任到期日 */
    private LocalDateTime endDate;
    /** 聘期年数 */
    private Integer termYears;
    /** 聘任文件附件 */
    private String appointmentDoc;
    /** 状态 hr_appointment_status */
    private String status;
    /** 是否当前有效聘任 */
    private Integer isCurrent;
    /** 备注 */
    private String remark;

}
