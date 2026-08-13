package vip.appap.suxin.module.hr.dal.mysql.resume;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumeRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.resume.ResumeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 履历 Mapper
 *
 * 说明：履历只写入 hr_resume 表，不双写 partner 表。
 * 查询时通过 LEFT JOIN partner 获取员工姓名。
 *
 * @author admin
 */
@Mapper
public interface ResumeMapper extends BaseMapperX<ResumeDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT r.id, r.partner_id, r.employee_no, r.dept, " +
            "r.resume_content, r.awards_punishments, r.certificates, r.papers, " +
            "r.annual_review, r.remark, r.province, r.city, r.county, " +
            "r.create_time, " +
            "p.name AS name, " +
            "d.name AS deptName " +
            "FROM hr_resume r " +
            "LEFT JOIN partner p ON p.id = r.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = r.dept AND d.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "r.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND r.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.employeeNo != null and reqVO.employeeNo != \"\"'>AND r.employee_no = #{reqVO.employeeNo}</if>" +
            "<if test='reqVO.dept != null and reqVO.dept != \"\"'>AND r.dept = #{reqVO.dept}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND r.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY r.id DESC</script>")
    IPage<ResumeRespVO> selectPageJoin(IPage<ResumeRespVO> page, @Param("reqVO") ResumePageReqVO reqVO);

    default PageResult<ResumeRespVO> selectPage(ResumePageReqVO reqVO) {
        IPage<ResumeRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<ResumeRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select(SELECT_JOIN_COLUMNS + "WHERE r.deleted = 0 AND r.id = #{id}")
    ResumeRespVO selectByIdJoin(@Param("id") Long id);

}
