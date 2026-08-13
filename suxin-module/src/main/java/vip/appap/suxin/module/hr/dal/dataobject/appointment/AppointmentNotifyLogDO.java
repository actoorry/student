package vip.appap.suxin.module.hr.dal.dataobject.appointment;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 聘期到期推送日志 DO（不继承 BaseDO，DDL 无审计列）
 */
@TableName("hr_appointment_notify_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentNotifyLogDO {

    @TableId
    private Long id;
    private Long appointmentId;
    private Long partnerId;
    private Long targetUserId;
    private String targetType;
    private String notifyType;
    private LocalDate referenceDate;
    private String notifyChannel;
    private String pushStatus;
    private String templateCode;
    private String contentSnapshot;
    private Long notifyMessageId;
    private String errorMsg;
    private LocalDateTime notifyTime;

}
