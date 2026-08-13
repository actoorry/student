package vip.appap.suxin.module.hr.dal.dataobject.positionchange;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * HR 岗位异动 DO
 */
@TableName("hr_position_change")
@KeySequence("hr_position_change_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionChangeDO extends BaseDO {

    @TableId
    private Long id;
    private Long partnerId;
    private String employeeNo;
    /** 异动类型 hr_position_change_type */
    private String changeType;
    private Long fromDept;
    private Long toDept;
    /** 原岗位编号，关联 system_post.id */
    private Long fromPostId;
    /** 新岗位编号，关联 system_post.id */
    private Long toPostId;
    /** 原岗位名称（快照） */
    private String fromPost;
    /** 新岗位名称（快照） */
    private String toPost;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private String docAttachment;
    /** 是否同步更新员工档案 */
    private Integer syncEmployee;
    private String remark;

}
