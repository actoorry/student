package vip.appap.suxin.module.hr.dal.dataobject.certificate;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 证书到期推送日志 DO
 *
 * 定时 Job 扫描 hr_certificate 中命中预警规则（到期前 30/60/90 天、考核前 60 天、已过期）
 * 的证书，向「本人 / 科室主任 / HR 管理员」推送站内信后，每条推送写一行日志。
 *
 * 去重键：(certificate_id, notify_type, reference_date, target_user_id)，
 * 仅当存在 push_status=success 的记录时视为已推送成功，failed/skipped 不阻断补推。
 *
 * 说明：本表为纯日志表，DDL 无 creator/updater/deleted 等审计字段，
 * 故不继承 BaseDO，避免 MyBatis-Plus 自动填充与软删除条件对不存在的列报错。
 * notify_time 由数据库 DEFAULT CURRENT_TIMESTAMP 自动填充。
 *
 * @author admin
 */
@TableName("hr_certificate_notify_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateNotifyLogDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联 hr_certificate.id
     */
    private Long certificateId;
    /**
     * 关联 partner.id（冗余便于排查）
     */
    private Long partnerId;
    /**
     * 推送目标 system_users.id
     */
    private Long targetUserId;
    /**
     * 目标类型：self 本人 / dept_leader 科室主任 / hr 人事科
     */
    private String targetType;
    /**
     * 通知类型：expired / expire_90 / expire_60 / expire_30 / assessment_60
     */
    private String notifyType;
    /**
     * 业务参考日（到期日或下次考核日）
     */
    private LocalDate referenceDate;
    /**
     * 推送渠道：internal 站内信 / zhiye 智业（预留）
     */
    private String notifyChannel;
    /**
     * 推送状态：success / failed / skipped
     */
    private String pushStatus;
    /**
     * 站内信模板编码
     */
    private String templateCode;
    /**
     * 渲染后消息摘要（审计用）
     */
    private String contentSnapshot;
    /**
     * system_notify_message.id（追溯）
     */
    private Long notifyMessageId;
    /**
     * 失败/跳过原因
     */
    private String errorMsg;
    /**
     * 推送时间（DB 默认 CURRENT_TIMESTAMP）
     */
    private LocalDateTime notifyTime;

}
