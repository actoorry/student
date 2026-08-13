package vip.appap.suxin.module.hr.job;

import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.appointment.AppointmentNotifyLogDO;
import vip.appap.suxin.module.hr.dal.mysql.appointment.AppointmentMapper;
import vip.appap.suxin.module.hr.dal.mysql.appointment.AppointmentNotifyLogMapper;
import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateMapper;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.framework.config.HrProperties;
import vip.appap.suxin.module.hr.service.appointment.AppointmentNotifyContentBuilder;
import vip.appap.suxin.module.hr.service.certificate.CertificateNotifyTargetResolver;
import vip.appap.suxin.module.system.service.NotifySendService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HR 聘期到期预警 Job（到期前 90 天）
 * beanName = appointmentNotifyJob
 */
@Component
@Slf4j
public class AppointmentNotifyJob implements JobHandler {

    public static final String TEMPLATE_EXPIRE_REMIND = "hr_appointment_expire_remind";
    public static final String LEDGER_URL = "/hr/appointment";
    public static final String QUERY_PERMISSION = "hr:appointment:query";

    @Resource
    private AppointmentMapper appointmentMapper;
    @Resource
    private AppointmentNotifyLogMapper notifyLogMapper;
    @Resource
    private NotifySendService notifySendService;
    @Resource
    private CertificateNotifyTargetResolver targetResolver;
    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private HrProperties hrProperties;

    @Override
    @TenantJob
    public String execute(String param) {
        LocalDate today = LocalDate.now();
        List<Long> hrUserIds = resolveHrUsers();
        int[] stats = new int[3];
        List<AppointmentRespVO> list = appointmentMapper.selectExpiringList(today, 90);
        for (AppointmentRespVO item : list) {
            if (item.getEndDate() != null) {
                pushToTargets(item, "expire_90", item.getEndDate().toLocalDate(), hrUserIds, stats);
            }
        }
        String result = String.format("推送 %d 条，跳过 %d 条，失败 %d 条", stats[0], stats[1], stats[2]);
        log.info("[AppointmentNotifyJob][{}]", result);
        return result;
    }

    private List<Long> resolveHrUsers() {
        List<Long> configured = hrProperties.getAppointment().getNotifyUserIds();
        if (configured != null && !configured.isEmpty()) {
            return configured;
        }
        List<Long> byPermission = certificateMapper.selectUserIdsByPermission(QUERY_PERMISSION);
        return byPermission != null ? byPermission : Collections.emptyList();
    }

    private void pushToTargets(AppointmentRespVO item, String notifyType, LocalDate referenceDate,
                                List<Long> hrUserIds, int[] stats) {
        String templateCode = AppointmentNotifyContentBuilder.templateCode(notifyType);
        Map<String, Object> params = AppointmentNotifyContentBuilder.buildParams(item, notifyType);

        Long selfId = targetResolver.resolveSelf(item.getPartnerId());
        if (selfId != null) {
            pushOne(item, notifyType, referenceDate, templateCode, params, selfId, "self", stats);
        } else if (item.getPartnerId() != null) {
            logOne(item, notifyType, referenceDate, "self", item.getPartnerId(), "skipped",
                    templateCode, AppointmentNotifyContentBuilder.snapshot(notifyType, item), "本人无系统账号", null);
            stats[1]++;
        }
        Long leaderId = targetResolver.resolveDeptLeader(item.getDept() != null ? item.getDept()
                : employeeMapper.selectDeptIdByPartnerId(item.getPartnerId()));
        if (leaderId != null) {
            pushOne(item, notifyType, referenceDate, templateCode, params, leaderId, "dept_leader", stats);
        }
        for (Long hrId : hrUserIds) {
            pushOne(item, notifyType, referenceDate, templateCode, params, hrId, "hr", stats);
        }
    }

    private void pushOne(AppointmentRespVO item, String notifyType, LocalDate referenceDate,
                         String templateCode, Map<String, Object> params, Long targetUserId,
                         String targetType, int[] stats) {
        if (notifyLogMapper.countSuccessByKey(item.getId(), notifyType, referenceDate, targetUserId) > 0) {
            stats[1]++;
            return;
        }
        String snapshot = AppointmentNotifyContentBuilder.snapshot(notifyType, item);
        try {
            Long messageId = notifySendService.sendSingleNotifyToAdmin(targetUserId, templateCode, params);
            if (messageId == null) {
                logOne(item, notifyType, referenceDate, targetType, targetUserId, "skipped",
                        templateCode, snapshot, "模板已关闭或未发送", null);
                stats[1]++;
            } else {
                logOne(item, notifyType, referenceDate, targetType, targetUserId, "success",
                        templateCode, snapshot, null, messageId);
                stats[0]++;
            }
        } catch (Exception e) {
            logOne(item, notifyType, referenceDate, targetType, targetUserId, "failed",
                    templateCode, snapshot, e.getMessage(), null);
            stats[2]++;
            log.error("[pushOne][推送失败 appointmentId={} target={}]", item.getId(), targetUserId, e);
        }
    }

    private void logOne(AppointmentRespVO item, String notifyType, LocalDate referenceDate,
                        String targetType, Long targetUserId, String pushStatus,
                        String templateCode, String contentSnapshot, String errorMsg, Long notifyMessageId) {
        try {
            AppointmentNotifyLogDO logDO = AppointmentNotifyLogDO.builder()
                    .appointmentId(item.getId())
                    .partnerId(item.getPartnerId())
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
            log.error("[logOne][写推送日志失败 appointmentId={}]", item.getId(), e);
        }
    }

}
