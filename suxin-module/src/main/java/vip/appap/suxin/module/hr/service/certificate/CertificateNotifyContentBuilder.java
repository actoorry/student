package vip.appap.suxin.module.hr.service.certificate;

import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.job.CertificateNotifyJob;

import java.util.HashMap;
import java.util.Map;

/**
 * 证书预警站内信内容构建工具
 *
 * 统一 Job 推送与「补推」重发的模板参数/建议文案/快照，避免两处逻辑漂移。
 *
 * notifyType：
 *   expire_90 / expire_60 / expire_30 — 到期前 N 天（模板 hr_certificate_expire_remind）
 *   assessment_60                     — 考核前 60 天（模板 hr_certificate_assessment_remind）
 *   expired                           — 已过期（模板 hr_certificate_expired）
 *
 * @author admin
 */
public final class CertificateNotifyContentBuilder {

    /** HR 督查台账直达链接（第一期静态路径，不做 SSO 深链） */
    public static final String LEDGER_URL = CertificateNotifyJob.LEDGER_URL;

    private CertificateNotifyContentBuilder() {
    }

    /**
     * 按通知类型返回站内信模板编码。
     */
    public static String templateCode(String notifyType) {
        switch (notifyType) {
            case "expired":
                return CertificateNotifyJob.TEMPLATE_EXPIRED;
            case "assessment_60":
                return CertificateNotifyJob.TEMPLATE_ASSESSMENT_REMIND;
            case "expire_90":
            case "expire_60":
            case "expire_30":
            default:
                return CertificateNotifyJob.TEMPLATE_EXPIRE_REMIND;
        }
    }

    /**
     * 按通知类型返回办理建议文案。
     */
    public static String suggestion(String notifyType) {
        switch (notifyType) {
            case "expire_90":
                return "证书将于 90 天后到期，请提前安排继续教育学分与延续注册准备。";
            case "expire_60":
                return "证书将于 60 天后到期，请尽快完成继续教育学分并准备延续注册申请。";
            case "expire_30":
                return "证书将于 30 天后到期，请立即提交延续注册/换证申请，以免过期影响执业。";
            case "assessment_60":
                return "定期考核将于 60 天后到期，请尽快完成考核并更新记录。";
            case "expired":
                return "证书已过期，请立即办理延续注册/换证，避免影响执业。";
            default:
                return "请及时处理证书相关事项。";
        }
    }

    /**
     * 按通知类型构建站内信模板参数。
     */
    public static Map<String, Object> buildParams(CertificateRespVO cert, String notifyType) {
        switch (notifyType) {
            case "expired":
                return buildExpiredParams(cert);
            case "assessment_60":
                return buildAssessmentParams(cert, suggestion(notifyType));
            case "expire_90":
            case "expire_60":
            case "expire_30":
            default:
                return buildExpireParams(cert, daysOf(notifyType), suggestion(notifyType));
        }
    }

    /**
     * 渲染后消息摘要（审计用，最长 500 字符）。
     */
    public static String snapshot(String notifyType, CertificateRespVO cert) {
        String s = "[" + notifyType + "] " + safe(cert.getName()) + " "
                + safe(cert.getCertificateName()) + " " + suggestion(notifyType) + " 链接: " + LEDGER_URL;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    private static int daysOf(String notifyType) {
        switch (notifyType) {
            case "expire_90": return 90;
            case "expire_60": return 60;
            case "expire_30": return 30;
            default: return 0;
        }
    }

    private static Map<String, Object> buildExpireParams(CertificateRespVO cert, int days, String suggestion) {
        Map<String, Object> params = new HashMap<>();
        params.put("employeeName", cert.getName() != null ? cert.getName() : "未知员工");
        params.put("certificateTypeName", cert.getCertificateName() != null ? cert.getCertificateName() : cert.getCertificateType());
        params.put("expireDate", String.valueOf(cert.getExpireDate()));
        params.put("days", String.valueOf(days));
        params.put("suggestion", suggestion);
        params.put("ledgerUrl", LEDGER_URL);
        return params;
    }

    private static Map<String, Object> buildAssessmentParams(CertificateRespVO cert, String suggestion) {
        Map<String, Object> params = new HashMap<>();
        params.put("employeeName", cert.getName() != null ? cert.getName() : "未知员工");
        params.put("certificateTypeName", cert.getCertificateName() != null ? cert.getCertificateName() : cert.getCertificateType());
        params.put("nextAssessmentDate", String.valueOf(cert.getNextAssessmentDate()));
        params.put("suggestion", suggestion);
        params.put("ledgerUrl", LEDGER_URL);
        return params;
    }

    private static Map<String, Object> buildExpiredParams(CertificateRespVO cert) {
        Map<String, Object> params = new HashMap<>();
        params.put("deptName", cert.getDeptName() != null ? cert.getDeptName() : "未分配科室");
        params.put("employeeName", cert.getName() != null ? cert.getName() : "未知员工");
        params.put("certificateTypeName", cert.getCertificateName() != null ? cert.getCertificateName() : cert.getCertificateType());
        params.put("certificateNo", cert.getCertificateNo() != null ? cert.getCertificateNo() : "");
        params.put("expireDate", String.valueOf(cert.getExpireDate()));
        return params;
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }
}
