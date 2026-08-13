package vip.appap.suxin.module.hr.service.certificate;

import vip.appap.suxin.module.hr.dal.mysql.certificate.CertificateMapper;
import vip.appap.suxin.module.hr.framework.config.HrProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 证书预警推送目标解析器
 *
 * 解析三类推送对象：
 * 1. self        — 本人（system_users.id = partner.id，需账号启用）
 * 2. dept_leader — 科室主任（system_dept.leader_user_id，需账号启用）
 * 3. hr          — HR 管理员（配置 suxin.hr.certificate.notify-user-ids 优先，否则按 hr:certificate:query 权限反查）
 *
 * @author admin
 */
@Component
public class CertificateNotifyTargetResolver {

    public static final String QUERY_PERMISSION = "hr:certificate:query";

    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private HrProperties hrProperties;

    /**
     * 解析本人 userId。无可用账号返回 null。
     */
    public Long resolveSelf(Long partnerId) {
        if (partnerId == null) {
            return null;
        }
        return certificateMapper.selectActiveUserIdByPartnerId(partnerId);
    }

    /**
     * 解析科室主任 userId。科室未配置主任或主任账号未启用返回 null。
     */
    public Long resolveDeptLeader(Long deptId) {
        if (deptId == null) {
            return null;
        }
        return certificateMapper.selectDeptLeaderUserId(deptId);
    }

    /**
     * 解析 HR 管理员 userId 列表。
     */
    public List<Long> resolveHrUsers() {
        List<Long> configured = hrProperties.getCertificate().getNotifyUserIds();
        if (configured != null && !configured.isEmpty()) {
            return configured;
        }
        List<Long> byPermission = certificateMapper.selectUserIdsByPermission(QUERY_PERMISSION);
        return byPermission != null ? byPermission : Collections.emptyList();
    }

}
