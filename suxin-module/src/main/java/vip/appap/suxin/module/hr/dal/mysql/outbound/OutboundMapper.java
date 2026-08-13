package vip.appap.suxin.module.hr.dal.mysql.outbound;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.outbound.OutboundDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * HR 人员外出记录 Mapper
 *
 * 查询时 LEFT JOIN partner 取姓名、LEFT JOIN hr_employee 取工号、
 * LEFT JOIN system_dept 取科室名。
 *
 * @author suxin
 */
@Mapper
public interface OutboundMapper extends BaseMapperX<OutboundDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT o.id, o.partner_id, o.record_type, o.province, o.city, o.county, " +
            "o.organization, o.practice_name, " +
            "o.start_date, o.end_date, o.duration_days, o.support_years, o.continuing_education_credit, " +
            "o.effective, o.summary, o.create_time, " +
            "p.name AS partnerName, e.employee_no AS employeeNo, d.name AS deptName " +
            "FROM hr_outbound o " +
            "LEFT JOIN partner p ON p.id = o.partner_id AND p.deleted = 0 " +
            "LEFT JOIN hr_employee e ON e.partner_id = o.partner_id AND e.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = e.dept AND d.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "o.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND o.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.recordType != null and reqVO.recordType != \"\"'>AND o.record_type = #{reqVO.recordType}</if>" +
            "<if test='reqVO.effective != null'>AND o.effective = #{reqVO.effective}</if>" +
            "<if test='reqVO.startDate != null and reqVO.startDate.length == 2'>AND o.start_date BETWEEN #{reqVO.startDate[0]} AND #{reqVO.startDate[1]}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND o.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY o.id DESC</script>")
    IPage<OutboundRespVO> selectPageJoin(IPage<OutboundRespVO> page, @Param("reqVO") OutboundPageReqVO reqVO);

    default PageResult<OutboundRespVO> selectPage(OutboundPageReqVO reqVO) {
        IPage<OutboundRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<OutboundRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select(SELECT_JOIN_COLUMNS + "WHERE o.deleted = 0 AND o.id = #{id}")
    OutboundRespVO selectByIdJoin(@Param("id") Long id);

    /**
     * 查询员工计入汇总的外出记录（用于档案汇总 Tab）
     */
    default List<OutboundDO> selectEffectiveListByPartnerId(Long partnerId) {
        return selectList(new LambdaQueryWrapperX<OutboundDO>()
                .eqIfPresent(OutboundDO::getPartnerId, partnerId)
                .eqIfPresent(OutboundDO::getEffective, 1)
                .orderByDesc(OutboundDO::getStartDate));
    }

}
