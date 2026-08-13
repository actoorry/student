package vip.appap.suxin.module.hr.dal.mysql.certificate;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.certificate.CertificateNotifyLogDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 证书到期推送日志 Mapper
 *
 * 说明：本表无 deleted 审计列，DO 不继承 BaseDO，故无逻辑删除过滤。
 * 查询显式列出日志列（不使用 l.*），避免与联表 c.expire_date 同名冲突。
 *
 * @author admin
 */
@Mapper
public interface CertificateNotifyLogMapper extends BaseMapperX<CertificateNotifyLogDO> {

    String LOG_COLUMNS =
            "l.id, l.certificate_id, l.partner_id, l.target_user_id, l.target_type, " +
            "l.notify_type, l.reference_date, l.notify_channel, l.push_status, " +
            "l.template_code, l.content_snapshot, l.notify_message_id, l.error_msg, l.notify_time ";

    /**
     * 去重查询：同一证书 + 同一规则 + 同一参考日 + 同一目标 是否已推送成功。
     * 仅 success 计入去重；failed/skipped 不阻断补推。
     */
    @Select("SELECT COUNT(1) FROM hr_certificate_notify_log " +
            "WHERE certificate_id = #{certificateId} " +
            "AND notify_type = #{notifyType} " +
            "AND reference_date = #{referenceDate} " +
            "AND target_user_id = #{targetUserId} " +
            "AND push_status = 'success'")
    int countSuccessByKey(@Param("certificateId") Long certificateId,
                          @Param("notifyType") String notifyType,
                          @Param("referenceDate") LocalDate referenceDate,
                          @Param("targetUserId") Long targetUserId);

    /**
     * 补推用：查询指定时间范围内推送失败的日志（按 notify_type 过滤）。
     */
    @Select("<script>SELECT " + LOG_COLUMNS +
            ", p.name AS target_name, c.certificate_name AS certificate_name, " +
            "c.certificate_no AS certificate_no, c.certificate_type AS certificate_type " +
            "FROM hr_certificate_notify_log l " +
            "LEFT JOIN partner p ON p.id = l.target_user_id AND p.deleted = 0 " +
            "LEFT JOIN hr_certificate c ON c.id = l.certificate_id AND c.deleted = 0 " +
            "WHERE l.push_status = 'failed' " +
            "AND l.notify_time &gt;= #{since} " +
            "<if test='notifyType != null and notifyType != \"\"'>AND l.notify_type = #{notifyType}</if> " +
            "ORDER BY l.notify_time ASC</script>")
    List<CertificateNotifyLogRespVO> selectFailedList(@Param("since") LocalDateTime since,
                                                     @Param("notifyType") String notifyType);

    /**
     * 推送日志分页（联表 partner 取目标姓名、hr_certificate 取证书信息）。
     */
    @Select("<script>SELECT " + LOG_COLUMNS +
            ", p.name AS target_name, c.certificate_name AS certificate_name, " +
            "c.certificate_no AS certificate_no, c.certificate_type AS certificate_type, " +
            "c.expire_date AS expire_date, c.next_assessment_date AS next_assessment_date " +
            "FROM hr_certificate_notify_log l " +
            "LEFT JOIN partner p ON p.id = l.target_user_id AND p.deleted = 0 " +
            "LEFT JOIN hr_certificate c ON c.id = l.certificate_id AND c.deleted = 0 " +
            "<where>" +
            "c.id IS NOT NULL " +
            "<if test='reqVO.notifyType != null and reqVO.notifyType != \"\"'>AND l.notify_type = #{reqVO.notifyType}</if>" +
            "<if test='reqVO.pushStatus != null and reqVO.pushStatus != \"\"'>AND l.push_status = #{reqVO.pushStatus}</if>" +
            "<if test='reqVO.targetType != null and reqVO.targetType != \"\"'>AND l.target_type = #{reqVO.targetType}</if>" +
            "<if test='reqVO.targetUserId != null'>AND l.target_user_id = #{reqVO.targetUserId}</if>" +
            "<if test='reqVO.certificateId != null'>AND l.certificate_id = #{reqVO.certificateId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.notifyTime != null and reqVO.notifyTime.length == 2'>AND l.notify_time BETWEEN #{reqVO.notifyTime[0]} AND #{reqVO.notifyTime[1]}</if>" +
            "</where>" +
            "ORDER BY l.notify_time DESC</script>")
    IPage<CertificateNotifyLogRespVO> selectPageJoin(IPage<CertificateNotifyLogRespVO> page,
                                                     @Param("reqVO") CertificateNotifyLogPageReqVO reqVO);

    default PageResult<CertificateNotifyLogRespVO> selectPage(CertificateNotifyLogPageReqVO reqVO) {
        IPage<CertificateNotifyLogRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<CertificateNotifyLogRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    /**
     * 补推成功后更新日志状态与消息 id。
     */
    @Update("UPDATE hr_certificate_notify_log SET push_status = #{pushStatus}, " +
            "notify_message_id = #{notifyMessageId}, error_msg = #{errorMsg}, " +
            "content_snapshot = #{contentSnapshot}, notify_time = NOW() WHERE id = #{id}")
    int updatePushResult(@Param("id") Long id,
                         @Param("pushStatus") String pushStatus,
                         @Param("notifyMessageId") Long notifyMessageId,
                         @Param("contentSnapshot") String contentSnapshot,
                         @Param("errorMsg") String errorMsg);

}
