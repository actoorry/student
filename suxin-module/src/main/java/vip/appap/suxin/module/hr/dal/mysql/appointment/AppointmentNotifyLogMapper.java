package vip.appap.suxin.module.hr.dal.mysql.appointment;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.appointment.AppointmentNotifyLogDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface AppointmentNotifyLogMapper extends BaseMapperX<AppointmentNotifyLogDO> {

    String LOG_COLUMNS =
            "l.id, l.appointment_id, l.partner_id, l.target_user_id, l.target_type, " +
            "l.notify_type, l.reference_date, l.notify_channel, l.push_status, " +
            "l.template_code, l.content_snapshot, l.notify_message_id, l.error_msg, l.notify_time ";

    @Select("SELECT COUNT(1) FROM hr_appointment_notify_log " +
            "WHERE appointment_id = #{appointmentId} " +
            "AND notify_type = #{notifyType} " +
            "AND reference_date = #{referenceDate} " +
            "AND target_user_id = #{targetUserId} " +
            "AND push_status = 'success'")
    int countSuccessByKey(@Param("appointmentId") Long appointmentId,
                          @Param("notifyType") String notifyType,
                          @Param("referenceDate") LocalDate referenceDate,
                          @Param("targetUserId") Long targetUserId);

    @Select("<script>SELECT " + LOG_COLUMNS +
            ", p.name AS targetName, ep.name AS partnerName, a.post_name AS postName, a.end_date AS endDate " +
            "FROM hr_appointment_notify_log l " +
            "LEFT JOIN partner p ON p.id = l.target_user_id AND p.deleted = 0 " +
            "LEFT JOIN hr_appointment a ON a.id = l.appointment_id AND a.deleted = 0 " +
            "LEFT JOIN partner ep ON ep.id = l.partner_id AND ep.deleted = 0 " +
            "<where>" +
            "a.id IS NOT NULL " +
            "<if test='reqVO.notifyType != null and reqVO.notifyType != \"\"'>AND l.notify_type = #{reqVO.notifyType}</if>" +
            "<if test='reqVO.pushStatus != null and reqVO.pushStatus != \"\"'>AND l.push_status = #{reqVO.pushStatus}</if>" +
            "<if test='reqVO.targetType != null and reqVO.targetType != \"\"'>AND l.target_type = #{reqVO.targetType}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.notifyTime != null and reqVO.notifyTime.length == 2'>AND l.notify_time BETWEEN #{reqVO.notifyTime[0]} AND #{reqVO.notifyTime[1]}</if>" +
            "</where>" +
            "ORDER BY l.notify_time DESC</script>")
    IPage<AppointmentNotifyLogRespVO> selectPageJoin(IPage<AppointmentNotifyLogRespVO> page,
                                                     @Param("reqVO") AppointmentNotifyLogPageReqVO reqVO);

    default PageResult<AppointmentNotifyLogRespVO> selectPage(AppointmentNotifyLogPageReqVO reqVO) {
        IPage<AppointmentNotifyLogRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<AppointmentNotifyLogRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

}
