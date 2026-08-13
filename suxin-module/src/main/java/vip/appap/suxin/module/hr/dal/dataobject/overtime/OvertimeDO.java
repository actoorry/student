package vip.appap.suxin.module.hr.dal.dataobject.overtime;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 人员加班 DO
 *
 * 记录大夜班/小夜班/法定节假日值班/周末值班/急诊加班/备班出勤/临时加班，用于登记台账与后续统计。
 * 第一期为纯登记台账，无 BPM 审批、无 status 字段。
 *
 * @author admin
 */
@TableName("hr_overtime")
@KeySequence("hr_overtime_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeDO extends BaseDO {

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
     * 员工工号（创建/更新时由员工档案回填）
     */
    private String employeeNo;
    /**
     * 员工所属部门（关联 system_dept.id，回填）
     */
    private Long dept;
    /**
     * 加班类型（字典 hr_overtime_type）
     */
    private String overtimeType;
    /**
     * 加班原因（字典 hr_overtime_reason）
     */
    private String overtimeReason;
    /**
     * 工作内容简述
     */
    private String workSummary;
    /**
     * 加班日期（统计按月汇总用；夜班填班次所属日）
     */
    private LocalDateTime overtimeDate;
    /**
     * 加班开始时间
     */
    private LocalDateTime startTime;
    /**
     * 加班结束时间（允许跨日，如夜班 22:00~次日06:00）
     */
    private LocalDateTime endTime;
    /**
     * 加班时长（小时，系统计算：end-start，保留1位小数）
     */
    private BigDecimal durationHours;
    /**
     * 实际值班科室（关联 system_dept.id，急诊加班等可不同于所属部门）
     */
    private Long workDeptId;
    /**
     * 值班地点，如「急诊科」「内一病区」
     */
    private String workLocation;
    /**
     * 节假日名称（仅 legal_holiday_duty 必填，如「2026年春节」）
     */
    private String holidayName;
    /**
     * 备注
     */
    private String remark;

}
