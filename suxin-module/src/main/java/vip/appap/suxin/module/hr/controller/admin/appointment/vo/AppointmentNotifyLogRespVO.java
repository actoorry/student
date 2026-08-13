package vip.appap.suxin.module.hr.controller.admin.appointment.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentNotifyLogRespVO {

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
    private String targetName;
    private String partnerName;
    private String postName;
    private LocalDateTime endDate;

}
