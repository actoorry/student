package vip.appap.suxin.module.hr.service.appointment;

import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentRespVO;
import vip.appap.suxin.module.hr.job.AppointmentNotifyJob;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class AppointmentNotifyContentBuilder {

    public static final String LEDGER_URL = AppointmentNotifyJob.LEDGER_URL;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private AppointmentNotifyContentBuilder() {
    }

    public static String templateCode(String notifyType) {
        return AppointmentNotifyJob.TEMPLATE_EXPIRE_REMIND;
    }

    public static String suggestion(String notifyType) {
        if ("expire_90".equals(notifyType)) {
            return "聘期将于 90 天后到期，请提前办理续聘手续，避免工资停发。";
        }
        return "请及时办理续聘手续。";
    }

    public static Map<String, Object> buildParams(AppointmentRespVO appointment, String notifyType) {
        Map<String, Object> params = new HashMap<>();
        params.put("employeeName", appointment.getPartnerName() != null ? appointment.getPartnerName() : "");
        params.put("postName", appointment.getPostName() != null ? appointment.getPostName() : "");
        params.put("days", appointment.getDaysToExpire() != null ? String.valueOf(appointment.getDaysToExpire()) : "90");
        params.put("endDate", appointment.getEndDate() != null ? DATE_FMT.format(appointment.getEndDate()) : "");
        params.put("suggestion", suggestion(notifyType));
        params.put("ledgerUrl", LEDGER_URL);
        return params;
    }

    public static String snapshot(String notifyType, AppointmentRespVO appointment) {
        return String.format("[%s] %s %s 到期日 %s",
                notifyType,
                appointment.getPartnerName(),
                appointment.getPostName(),
                appointment.getEndDate() != null ? DATE_FMT.format(appointment.getEndDate()) : "-");
    }

}
