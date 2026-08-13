package vip.appap.suxin.module.hr.dal.mysql.positionchange;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.positionchange.vo.PositionChangePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.positionchange.vo.PositionChangeRespVO;
import vip.appap.suxin.module.hr.controller.admin.positionchange.vo.PositionTimelineRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.positionchange.PositionChangeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PositionChangeMapper extends BaseMapperX<PositionChangeDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT c.id, c.partner_id, c.employee_no, c.change_type, c.from_dept, c.to_dept, " +
            "c.from_post_id, c.to_post_id, " +
            "COALESCE(fp.name, c.from_post) AS fromPost, COALESCE(tp.name, c.to_post) AS toPost, " +
            "c.start_date, c.end_date, c.reason, c.doc_attachment, " +
            "c.sync_employee, c.remark, c.create_time, " +
            "p.name AS partnerName, fd.name AS fromDeptName, td.name AS toDeptName " +
            "FROM hr_position_change c " +
            "LEFT JOIN partner p ON p.id = c.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept fd ON fd.id = c.from_dept AND fd.deleted = 0 " +
            "LEFT JOIN system_dept td ON td.id = c.to_dept AND td.deleted = 0 " +
            "LEFT JOIN system_post fp ON fp.id = c.from_post_id AND fp.deleted = 0 " +
            "LEFT JOIN system_post tp ON tp.id = c.to_post_id AND tp.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "c.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND c.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.changeType != null and reqVO.changeType != \"\"'>AND c.change_type = #{reqVO.changeType}</if>" +
            "<if test='reqVO.startDate != null and reqVO.startDate.length == 2'>AND c.start_date BETWEEN #{reqVO.startDate[0]} AND #{reqVO.startDate[1]}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND c.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY c.id DESC</script>")
    IPage<PositionChangeRespVO> selectPageJoin(IPage<PositionChangeRespVO> page,
                                               @Param("reqVO") PositionChangePageReqVO reqVO);

    default PageResult<PositionChangeRespVO> selectPage(PositionChangePageReqVO reqVO) {
        IPage<PositionChangeRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<PositionChangeRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select("<script>" + SELECT_JOIN_COLUMNS + "WHERE c.deleted = 0 AND c.id = #{id}</script>")
    PositionChangeRespVO selectByIdJoin(@Param("id") Long id);

    @Select("<script>" +
            "SELECT * FROM (" +
            "SELECT 'position_change' AS sourceType, c.change_type AS changeType, " +
            "CONCAT('岗位异动：', c.change_type) AS title, " +
            "fd.name AS fromDeptName, td.name AS toDeptName, " +
            "c.from_post_id AS fromPostId, c.to_post_id AS toPostId, " +
            "COALESCE(fp.name, c.from_post) AS fromPost, COALESCE(tp.name, c.to_post) AS toPost, " +
            "c.start_date AS startDate, c.end_date AS endDate, c.reason AS remark " +
            "FROM hr_position_change c " +
            "LEFT JOIN system_dept fd ON fd.id = c.from_dept AND fd.deleted = 0 " +
            "LEFT JOIN system_dept td ON td.id = c.to_dept AND td.deleted = 0 " +
            "LEFT JOIN system_post fp ON fp.id = c.from_post_id AND fp.deleted = 0 " +
            "LEFT JOIN system_post tp ON tp.id = c.to_post_id AND tp.deleted = 0 " +
            "WHERE c.deleted = 0 AND c.partner_id = #{partnerId} " +
            "UNION ALL " +
            "SELECT 'outbound' AS sourceType, o.record_type AS changeType, " +
            "COALESCE(o.practice_name, o.organization, '外出记录') AS title, " +
            "NULL AS fromDeptName, d.name AS toDeptName, " +
            "NULL AS fromPostId, NULL AS toPostId, NULL AS fromPost, NULL AS toPost, " +
            "o.start_date AS startDate, o.end_date AS endDate, o.summary AS remark " +
            "FROM hr_outbound o " +
            "LEFT JOIN hr_employee e ON e.partner_id = o.partner_id AND e.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = e.dept AND d.deleted = 0 " +
            "WHERE o.deleted = 0 AND o.partner_id = #{partnerId} " +
            ") t ORDER BY t.startDate DESC</script>")
    List<PositionTimelineRespVO> selectTimelineByPartnerId(@Param("partnerId") Long partnerId);

}
