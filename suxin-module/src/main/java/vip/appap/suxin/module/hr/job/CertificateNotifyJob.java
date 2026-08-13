package vip.appap.suxin.module.hr.job;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.certificate.CertificateNotifyLogDO;
import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateMapper;
import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateNotifyLogMapper;
import vip.appap.suxin.module.hr.service.certificate.CertificateNotifyContentBuilder;
import vip.appap.suxin.module.hr.service.certificate.CertificateNotifyTargetResolver;
import vip.appap.suxin.module.system.service.NotifySendService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * HR 证书预警站内信推送 Job
 *
 * 每日扫描 hr_certificate，命中以下规则时向「本人 + 科室主任 + HR 管理员」推送站内信：
 *   expire_90     : 到期前 90 天
 *   expire_60     : 到期前 60 天
 *   expire_30     : 到期前 30 天
 *   assessment_60 : 下次考核前 60 天
 *   expired       : 已过期
 *
 * 去重：(certificate_id, notify_type, reference_date, target_user_id) 且 push_status=success。
 * 每条推送写一行 hr_certificate_notify_log，failed/skipped 不阻断次日补推。
 *
 * cron 由 infra_job 配置，建议 0 0 8 * * ?（每日 8:00）。
 * beanName = certificateNotifyJob。
 *
 * @author admin
 */
@Component
@Slf4j
public class CertificateNotifyJob implements JobHandler {

    public static final String TEMPLATE_EXPIRE_REMIND = "hr_certificate_expire_remind";
    public static final String TEMPLATE_ASSESSMENT_REMIND = "hr_certificate_assessment_remind";
    public static final String TEMPLATE_EXPIRED = "hr_certificate_expired";

    /** HR 督查台账直达链接（第一期静态路径，不做 SSO 深链） */
    public static final String LEDGER_URL = "/hr/certificate/ledger";

    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private CertificateNotifyLogMapper notifyLogMapper;
    @Resource
    private NotifySendService notifySendService;
    @Resource
    private CertificateNotifyTargetResolver targetResolver;

    @Override
    @TenantJob
    public String execute(String param) {
        LocalDate today = LocalDate.now();
        List<Long> hrUserIds = targetResolver.resolveHrUsers();
        if (hrUserIds.isEmpty()) {
            log.warn("[execute][无 HR 证书管理权限用户，可在 suxin.hr.certificate.notify-user-ids 配置固定 userId]");
        }
        int[] stats = new int[3]; // 0=pushed, 1=skipped, 2=failed

        processExpireRule(today, 90, "expire_90", hrUserIds, stats);
        processExpireRule(today, 60, "expire_60", hrUserIds, stats);
        processExpireRule(today, 30, "expire_30", hrUserIds, stats);
        processAssessmentRule(today, 60, "assessment_60", hrUserIds, stats);
        processExpiredRule(today, hrUserIds, stats);

        String result = String.format("推送 %d 条，跳过 %d 条，失败 %d 条", stats[0], stats[1], stats[2]);
        log.info("[execute][{}]", result);
        return result;
    }

    private void processExpireRule(LocalDate today, int days, String notifyType,
                                   List<Long> hrUserIds, int[] stats) {
        List<CertificateRespVO> list = certificateMapper.selectExpiringList(today, days);
        if (list.isEmpty()) {
            return;
        }
        for (CertificateRespVO cert : list) {
            if (cert.getExpireDate() != null) {
                pushToTargets(cert, notifyType, cert.getExpireDate().toLocalDate(), hrUserIds, stats);
            }
        }
    }

    private void processAssessmentRule(LocalDate today, int days, String notifyType,
                                       List<Long> hrUserIds, int[] stats) {
        List<CertificateRespVO> list = certificateMapper.selectAssessmentDueList(today, days);
        if (list.isEmpty()) {
            return;
        }
        for (CertificateRespVO cert : list) {
            if (cert.getNextAssessmentDate() != null) {
                pushToTargets(cert, notifyType, cert.getNextAssessmentDate().toLocalDate(), hrUserIds, stats);
            }
        }
    }

    private void processExpiredRule(LocalDate today, List<Long> hrUserIds, int[] stats) {
        List<CertificateRespVO> list = certificateMapper.selectExpiredList(today);
        if (list.isEmpty()) {
            return;
        }
        for (CertificateRespVO cert : list) {
            if (cert.getExpireDate() != null) {
                pushToTargets(cert, "expired", cert.getExpireDate().toLocalDate(), hrUserIds, stats);
            }
        }
    }

    /**
     * 向本人 / 科室主任 / HR 管理员推送，逐条写日志并去重。
     */
    private void pushToTargets(CertificateRespVO cert, String notifyType, LocalDate referenceDate,
                               List<Long> hrUserIds, int[] stats) {
        String templateCode = CertificateNotifyContentBuilder.templateCode(notifyType);
        Map<String, Object> params = CertificateNotifyContentBuilder.buildParams(cert, notifyType);

        // 本人
        Long selfId = targetResolver.resolveSelf(cert.getPartnerId());
        if (selfId != null) {
            pushOne(cert, notifyType, referenceDate, templateCode, params, selfId, "self", stats);
        } else if (cert.getPartnerId() != null) {
            logOne(cert, notifyType, referenceDate, "self", cert.getPartnerId(), "skipped",
                    templateCode, CertificateNotifyContentBuilder.snapshot(notifyType, cert), "本人无系统账号", null);
            stats[1]++;
        }
        // 科室主任
        Long leaderId = targetResolver.resolveDeptLeader(cert.getDept());
        if (leaderId != null) {
            pushOne(cert, notifyType, referenceDate, templateCode, params, leaderId, "dept_leader", stats);
        }
        // HR 管理员
        for (Long hrId : hrUserIds) {
            pushOne(cert, notifyType, referenceDate, templateCode, params, hrId, "hr", stats);
        }
    }

    private void pushOne(CertificateRespVO cert, String notifyType, LocalDate referenceDate,
                         String templateCode, Map<String, Object> params, Long targetUserId,
                         String targetType, int[] stats) {
        if (notifyLogMapper.countSuccessByKey(cert.getId(), notifyType, referenceDate, targetUserId) > 0) {
            stats[1]++;
            return;
        }
        String snapshot = CertificateNotifyContentBuilder.snapshot(notifyType, cert);
        try {
            Long messageId = notifySendService.sendSingleNotifyToAdmin(targetUserId, templateCode, params);
            if (messageId == null) {
                logOne(cert, notifyType, referenceDate, targetType, targetUserId, "skipped",
                        templateCode, snapshot, "模板已关闭或未发送", null);
                stats[1]++;
            } else {
                logOne(cert, notifyType, referenceDate, targetType, targetUserId, "success",
                        templateCode, snapshot, null, messageId);
                stats[0]++;
            }
        } catch (Exception e) {
            logOne(cert, notifyType, referenceDate, targetType, targetUserId, "failed",
                    templateCode, snapshot, e.getMessage(), null);
            stats[2]++;
            log.error("[pushOne][推送失败 certId={} type={} target={}]", cert.getId(), notifyType, targetUserId, e);
        }
    }

    private void logOne(CertificateRespVO cert, String notifyType, LocalDate referenceDate,
                        String targetType, Long targetUserId, String pushStatus,
                        String templateCode, String contentSnapshot, String errorMsg, Long notifyMessageId) {
        try {
            CertificateNotifyLogDO logDO = CertificateNotifyLogDO.builder()
                    .certificateId(cert.getId())
                    .partnerId(cert.getPartnerId())
                    .targetUserId(targetUserId)
                    .targetType(targetType)
                    .notifyType(notifyType)
                    .referenceDate(referenceDate)
                    .notifyChannel("internal")
                    .pushStatus(pushStatus)
                    .templateCode(templateCode)
                    .contentSnapshot(contentSnapshot)
                    .notifyMessageId(notifyMessageId)
                    .errorMsg(errorMsg)
                    .build();
            notifyLogMapper.insert(logDO);
        } catch (Exception e) {
            log.error("[logOne][写推送日志失败 certId={} type={} target={}]", cert.getId(), notifyType, targetUserId, e);
        }
    }
}
