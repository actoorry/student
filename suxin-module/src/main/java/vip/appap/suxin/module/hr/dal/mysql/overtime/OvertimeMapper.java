package vip.appap.suxin.module.hr.dal.mysql.overtime;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.OvertimePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.OvertimeRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.overtime.OvertimeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 人员加班 Mapper
 *
 * 说明：加班只写入 hr_overtime 表。查询时 LEFT JOIN partner 取姓名、
 * LEFT JOIN system_dept 两次：一次取所属科室名（o.dept），一次取值班科室名（o.work_dept_id）。
 *
 * @author admin
 */
@Mapper
public interface OvertimeMapper extends BaseMapperX<OvertimeDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT o.id, o.partner_id, o.employee_no, o.dept, " +
            "o.overtime_type, o.overtime_reason, o.work_summary, o.overtime_date, o.start_time, o.end_time, o.duration_hours, " +
            "o.work_dept_id, o.work_location, o.holiday_name, o.remark, o.create_time, " +
            "p.name AS name, " +
            "d.name AS dept_name, " +
            "wd.name AS work_dept_name " +
            "FROM hr_overtime o " +
            "LEFT JOIN partner p ON p.id = o.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = o.dept AND d.deleted = 0 " +
            "LEFT JOIN system_dept wd ON wd.id = o.work_dept_id AND wd.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "o.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND o.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.overtimeType != null and reqVO.overtimeType != \"\"'>AND o.overtime_type = #{reqVO.overtimeType}</if>" +
            "<if test='reqVO.overtimeReason != null and reqVO.overtimeReason != \"\"'>AND o.overtime_reason = #{reqVO.overtimeReason}</if>" +
            "<if test='reqVO.dept != null'>AND o.dept = #{reqVO.dept}</if>" +
            "<if test='reqVO.workDeptId != null'>AND o.work_dept_id = #{reqVO.workDeptId}</if>" +
            "<if test='reqVO.overtimeDate != null and reqVO.overtimeDate.length == 2'>AND o.overtime_date BETWEEN #{reqVO.overtimeDate[0]} AND #{reqVO.overtimeDate[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY o.id DESC</script>")
    IPage<OvertimeRespVO> selectPageJoin(IPage<OvertimeRespVO> page, @Param("reqVO") OvertimePageReqVO reqVO);

    default PageResult<OvertimeRespVO> selectPage(OvertimePageReqVO reqVO) {
        IPage<OvertimeRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<OvertimeRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE o.deleted = 0 AND o.id = #{id}</script>")
    OvertimeRespVO selectByIdJoin(@Param("id") Long id);

}
