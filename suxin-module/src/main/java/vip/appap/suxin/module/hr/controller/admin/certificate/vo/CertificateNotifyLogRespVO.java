package vip.appap.suxin.module.hr.controller.admin.certificate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 证书推送日志 Response VO")
@Data
public class CertificateNotifyLogRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "关联 hr_certificate.id")
    private Long certificateId;

    @Schema(description = "关联 partner.id（冗余）")
    private Long partnerId;

    @Schema(description = "推送目标 system_users.id")
    private Long targetUserId;

    @Schema(description = "目标类型：self / dept_leader / hr")
    private String targetType;

    @Schema(description = "通知类型：expired / expire_90 / expire_60 / expire_30 / assessment_60")
    private String notifyType;

    @Schema(description = "业务参考日")
    private LocalDate referenceDate;

    @Schema(description = "推送渠道：internal / zhiye")
    private String notifyChannel;

    @Schema(description = "推送状态：success / failed / skipped")
    private String pushStatus;

    @Schema(description = "站内信模板编码")
    private String templateCode;

    @Schema(description = "渲染后消息摘要")
    private String contentSnapshot;

    @Schema(description = "system_notify_message.id")
    private Long notifyMessageId;

    @Schema(description = "失败/跳过原因")
    private String errorMsg;

    @Schema(description = "推送时间")
    private LocalDateTime notifyTime;

    // ===== 联表字段 =====

    @Schema(description = "目标用户姓名（联表 partner.name）")
    private String targetName;

    @Schema(description = "证书名称")
    private String certificateName;

    @Schema(description = "证书编号")
    private String certificateNo;

    @Schema(description = "证书类型")
    private String certificateType;

    @Schema(description = "到期日期")
    private LocalDate expireDate;

    @Schema(description = "下次考核日")
    private LocalDate nextAssessmentDate;

}
