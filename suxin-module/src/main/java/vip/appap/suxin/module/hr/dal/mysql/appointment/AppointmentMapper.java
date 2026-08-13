package vip.appap.suxin.module.hr.dal.mysql.appointment;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.appointment.AppointmentDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface AppointmentMapper extends BaseMapperX<AppointmentDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT a.id, a.partner_id, a.employee_no, a.dept, a.post_category, a.post_level, a.post_id, " +
            "COALESCE(sp.name, a.post_name) AS postName, " +
            "a.start_date, a.end_date, a.term_years, a.appointment_doc, a.status, a.is_current, a.remark, a.create_time, " +
            "p.name AS partnerName, d.name AS deptName, " +
            "DATEDIFF(a.end_date, CURDATE()) AS daysToExpire " +
            "FROM hr_appointment a " +
            "LEFT JOIN partner p ON p.id = a.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = a.dept AND d.deleted = 0 " +
            "LEFT JOIN system_post sp ON sp.id = a.post_id AND sp.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "a.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND a.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.postCategory != null and reqVO.postCategory != \"\"'>AND a.post_category = #{reqVO.postCategory}</if>" +
            "<if test='reqVO.postLevel != null and reqVO.postLevel != \"\"'>AND a.post_level = #{reqVO.postLevel}</if>" +
            "<if test='reqVO.status != null and reqVO.status != \"\"'>AND a.status = #{reqVO.status}</if>" +
            "<if test='reqVO.isCurrent != null'>AND a.is_current = #{reqVO.isCurrent}</if>" +
            "<if test='reqVO.endDate != null and reqVO.endDate.length == 2'>AND a.end_date BETWEEN #{reqVO.endDate[0]} AND #{reqVO.endDate[1]}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND a.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY a.id DESC</script>")
    IPage<AppointmentRespVO> selectPageJoin(IPage<AppointmentRespVO> page, @Param("reqVO") AppointmentPageReqVO reqVO);

    default PageResult<AppointmentRespVO> selectPage(AppointmentPageReqVO reqVO) {
        IPage<AppointmentRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<AppointmentRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE a.deleted = 0 AND a.id = #{id}</script>")
    AppointmentRespVO selectByIdJoin(@Param("id") Long id);

    @Update("UPDATE hr_appointment SET is_current = 0 WHERE partner_id = #{partnerId} AND deleted = 0")
    int clearCurrentByPartnerId(@Param("partnerId") Long partnerId);

    @Select("<script>" + SELECT_JOIN_COLUMNS +
            "WHERE a.deleted = 0 AND a.status = 'active' AND a.is_current = 1 " +
            "AND a.end_date IS NOT NULL AND DATEDIFF(a.end_date, #{today}) = #{days}</script>")
    List<AppointmentRespVO> selectExpiringList(@Param("today") LocalDate today, @Param("days") int days);

    default List<AppointmentDO> selectCurrentListByPartnerId(Long partnerId) {
        return selectList(new LambdaQueryWrapperX<AppointmentDO>()
                .eq(AppointmentDO::getPartnerId, partnerId)
                .eq(AppointmentDO::getIsCurrent, 1)
                .orderByDesc(AppointmentDO::getStartDate));
    }

}
