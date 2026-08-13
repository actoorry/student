package vip.appap.suxin.module.hr.dal.mysql.certificate;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateLedgerSummaryRespVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificatePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.certificate.CertificateDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 人员证书 Mapper
 *
 * 说明：证书只写入 hr_certificate 表。查询时 LEFT JOIN partner 取姓名、
 * LEFT JOIN system_dept 取科室名。状态字段由 SQL 动态计算（基于 expire_date / next_assessment_date）。
 *
 * 状态定义（SQL CASE 动态计算，单值优先级从上到下）：
 *   expired            : 已过期（expire_date 早于今天）
 *   expiring           : 30 天内到期（含今天，至今天+30）
 *   assessment_overdue : 考核逾期（next_assessment_date 早于今天）
 *   expiring_60        : 31~60 天内到期
 *   expiring_90        : 61~90 天内到期
 *   assessment_due_60  : 考核前 60 天（next_assessment_date 在今天至今天+60）
 *   valid              : 未命中以上任一
 *
 * 督查台账汇总计数为累计口径：30/60/90 天卡片均为「今天起 N 天内到期」，
 * 故 60 天计数包含 30 天、90 天计数包含 30/60 天。
 *
 * @author admin
 */
@Mapper
public interface CertificateMapper extends BaseMapperX<CertificateDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT c.id, c.partner_id, c.employee_no, c.dept, " +
            "c.certificate_type, c.certificate_name, c.certificate_no, c.issuing_authority, " +
            "c.issue_date, c.expire_date, c.last_assessment_date, c.next_assessment_date, " +
            "c.attachment, c.remark, c.create_time, " +
            "p.name AS name, " +
            "d.name AS dept_name, " +
            "CASE " +
            "  WHEN c.expire_date IS NOT NULL AND c.expire_date &lt; CURDATE() THEN 'expired' " +
            "  WHEN c.expire_date IS NOT NULL AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'expiring' " +
            "  WHEN c.next_assessment_date IS NOT NULL AND c.next_assessment_date &lt; CURDATE() THEN 'assessment_overdue' " +
            "  WHEN c.expire_date IS NOT NULL AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY) THEN 'expiring_60' " +
            "  WHEN c.expire_date IS NOT NULL AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 90 DAY) THEN 'expiring_90' " +
            "  WHEN c.next_assessment_date IS NOT NULL AND c.next_assessment_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY) THEN 'assessment_due_60' " +
            "  ELSE 'valid' " +
            "END AS status " +
            "FROM hr_certificate c " +
            "LEFT JOIN partner p ON p.id = c.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = c.dept AND d.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "c.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND c.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.certificateType != null and reqVO.certificateType != \"\"'>AND c.certificate_type = #{reqVO.certificateType}</if>" +
            "<if test='reqVO.dept != null'>AND c.dept = #{reqVO.dept}</if>" +
            "<if test='reqVO.expireDate != null and reqVO.expireDate.length == 2'>AND c.expire_date BETWEEN #{reqVO.expireDate[0]} AND #{reqVO.expireDate[1]}</if>" +
            "<if test='reqVO.missingAttachment != null and reqVO.missingAttachment == true'>AND (c.attachment IS NULL OR c.attachment = '')</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND c.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"expired\"'>AND c.expire_date IS NOT NULL AND c.expire_date &lt; CURDATE()</if>" +
            "<if test='reqVO.status != null and (reqVO.status == \"expiring\" or reqVO.status == \"expiring_30\")'>AND c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 30 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"expiring_60\"'>AND c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"expiring_90\"'>AND c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 90 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"assessment_overdue\"'>AND c.next_assessment_date IS NOT NULL AND c.next_assessment_date &lt; CURDATE()</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"assessment_due_60\"'>AND c.next_assessment_date IS NOT NULL AND c.next_assessment_date &gt;= CURDATE() AND c.next_assessment_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"valid\"'>AND (c.expire_date IS NULL OR c.expire_date &gt; DATE_ADD(CURDATE(), INTERVAL 90 DAY)) AND (c.next_assessment_date IS NULL OR c.next_assessment_date &gt; DATE_ADD(CURDATE(), INTERVAL 60 DAY))</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY c.id DESC</script>")
    IPage<CertificateRespVO> selectPageJoin(IPage<CertificateRespVO> page, @Param("reqVO") CertificatePageReqVO reqVO);

    default PageResult<CertificateRespVO> selectPage(CertificatePageReqVO reqVO) {
        IPage<CertificateRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<CertificateRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE c.deleted = 0 AND c.id = #{id}</script>")
    CertificateRespVO selectByIdJoin(@Param("id") Long id);

    /**
     * 督查台账专用：默认筛选「已过期 OR 90 天内到期 OR 考核逾期 OR 考核前 60 天」的并集，
     * 支持额外叠加 name/dept/certificateType/missingAttachment/status。
     */
    String WHERE_LEDGER =
            "<where>" +
            "c.deleted = 0 " +
            "AND ( " +
            "  (c.expire_date IS NOT NULL AND c.expire_date &lt; CURDATE()) " +
            "  OR (c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 90 DAY)) " +
            "  OR (c.next_assessment_date IS NOT NULL AND c.next_assessment_date &lt; CURDATE()) " +
            "  OR (c.next_assessment_date IS NOT NULL AND c.next_assessment_date &gt;= CURDATE() AND c.next_assessment_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY)) " +
            ") " +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.certificateType != null and reqVO.certificateType != \"\"'>AND c.certificate_type = #{reqVO.certificateType}</if>" +
            "<if test='reqVO.dept != null'>AND c.dept = #{reqVO.dept}</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"expired\"'>AND c.expire_date IS NOT NULL AND c.expire_date &lt; CURDATE()</if>" +
            "<if test='reqVO.status != null and (reqVO.status == \"expiring\" or reqVO.status == \"expiring_30\")'>AND c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 30 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"expiring_60\"'>AND c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"expiring_90\"'>AND c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 90 DAY)</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"assessment_overdue\"'>AND c.next_assessment_date IS NOT NULL AND c.next_assessment_date &lt; CURDATE()</if>" +
            "<if test='reqVO.status != null and reqVO.status == \"assessment_due_60\"'>AND c.next_assessment_date IS NOT NULL AND c.next_assessment_date &gt;= CURDATE() AND c.next_assessment_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY)</if>" +
            "<if test='reqVO.missingAttachment != null and reqVO.missingAttachment == true'>AND (c.attachment IS NULL OR c.attachment = '')</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_LEDGER + "ORDER BY c.expire_date ASC</script>")
    IPage<CertificateRespVO> selectLedgerPageJoin(IPage<CertificateRespVO> page, @Param("reqVO") CertificatePageReqVO reqVO);

    default PageResult<CertificateRespVO> selectLedgerPage(CertificatePageReqVO reqVO) {
        IPage<CertificateRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<CertificateRespVO> result = selectLedgerPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    /**
     * 督查台账汇总：返回 expired / expiring(30天) / expiring60 / expiring90 / assessmentOverdue / assessmentDue60 计数。
     * 到期口径为累计：expiring60 含 30 天、expiring90 含 30/60 天。
     */
    @Select("<script>SELECT " +
            "SUM(CASE WHEN c.expire_date IS NOT NULL AND c.expire_date &lt; CURDATE() THEN 1 ELSE 0 END) AS expiredCount, " +
            "SUM(CASE WHEN c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) AS expiringCount, " +
            "SUM(CASE WHEN c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY) THEN 1 ELSE 0 END) AS expiring60Count, " +
            "SUM(CASE WHEN c.expire_date IS NOT NULL AND c.expire_date &gt;= CURDATE() AND c.expire_date &lt;= DATE_ADD(CURDATE(), INTERVAL 90 DAY) THEN 1 ELSE 0 END) AS expiring90Count, " +
            "SUM(CASE WHEN c.next_assessment_date IS NOT NULL AND c.next_assessment_date &lt; CURDATE() THEN 1 ELSE 0 END) AS assessmentOverdueCount, " +
            "SUM(CASE WHEN c.next_assessment_date IS NOT NULL AND c.next_assessment_date &gt;= CURDATE() AND c.next_assessment_date &lt;= DATE_ADD(CURDATE(), INTERVAL 60 DAY) THEN 1 ELSE 0 END) AS assessmentDue60Count " +
            "FROM hr_certificate c WHERE c.deleted = 0</script>")
    CertificateLedgerSummaryRespVO selectLedgerSummary();

    /**
     * 定时 Job 用：查询所有已过期（expire_date &lt; 今天）的证书。
     */
    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE c.deleted = 0 AND c.expire_date IS NOT NULL AND c.expire_date &lt; #{today} ORDER BY c.expire_date ASC</script>")
    List<CertificateRespVO> selectExpiredList(@Param("today") LocalDate today);

    /**
     * 定时 Job 用：查询到期日恰好等于今天 + N 天的证书（用于到期前 N 天提醒）。
     */
    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE c.deleted = 0 AND c.expire_date IS NOT NULL AND c.expire_date = DATE_ADD(#{today}, INTERVAL #{days} DAY) ORDER BY c.expire_date ASC</script>")
    List<CertificateRespVO> selectExpiringList(@Param("today") LocalDate today, @Param("days") int days);

    /**
     * 定时 Job 用：查询下次考核日恰好等于今天 + N 天的证书（用于考核前 N 天提醒）。
     */
    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE c.deleted = 0 AND c.next_assessment_date IS NOT NULL AND c.next_assessment_date = DATE_ADD(#{today}, INTERVAL #{days} DAY) ORDER BY c.next_assessment_date ASC</script>")
    List<CertificateRespVO> selectAssessmentDueList(@Param("today") LocalDate today, @Param("days") int days);

    /**
     * 定时 Job 用：查询拥有指定权限的管理员用户 id 列表（用于站内信推送对象）。
     * JOIN system_menu -> system_role_menu -> system_user_role -> system_users
     */
    @Select("SELECT DISTINCT ur.user_id FROM system_user_role ur " +
            "JOIN system_role_menu rm ON rm.role_id = ur.role_id AND rm.deleted = 0 " +
            "JOIN system_menu m ON m.id = rm.menu_id AND m.deleted = 0 AND m.permission = #{permission} " +
            "JOIN system_users u ON u.id = ur.user_id AND u.deleted = 0 AND u.status = 0 " +
            "WHERE ur.deleted = 0")
    List<Long> selectUserIdsByPermission(@Param("permission") String permission);

    /**
     * 定时 Job 用：按 partnerId 查询本人是否拥有可用的系统账号（system_users.id = partner.id）。
     * 存在且 status=0 返回 userId，否则返回 null。
     */
    @Select("SELECT u.id FROM system_users u " +
            "WHERE u.id = #{partnerId} AND u.deleted = 0 AND u.status = 0 LIMIT 1")
    Long selectActiveUserIdByPartnerId(@Param("partnerId") Long partnerId);

    /**
     * 定时 Job 用：查询科室主任的 system_users.id（system_dept.leader_user_id）。
     * 仅当主任存在且账号启用时返回，否则 null。
     */
    @Select("SELECT d.leader_user_id FROM system_dept d " +
            "JOIN system_users u ON u.id = d.leader_user_id AND u.deleted = 0 AND u.status = 0 " +
            "WHERE d.id = #{deptId} AND d.deleted = 0 AND d.leader_user_id IS NOT NULL LIMIT 1")
    Long selectDeptLeaderUserId(@Param("deptId") Long deptId);

}
