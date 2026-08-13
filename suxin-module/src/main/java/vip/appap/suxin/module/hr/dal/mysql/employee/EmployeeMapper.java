package vip.appap.suxin.module.hr.dal.mysql.employee;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeeRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.employee.EmployeeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 员工 Mapper
 *
 * @author admin
 */
@Mapper
public interface EmployeeMapper extends BaseMapperX<EmployeeDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT e.id, e.partner_id, e.employee_no, e.employee_mobile, e.dept, e.post_id, e.personnel_category, " +
            "e.professional_title, e.political_status, e.party_join_date, e.career_start_date, " +
            "e.hire_date, e.position, e.duty, e.appointment_date, e.highest_education, " +
            "e.full_time_education, e.establishment_status, e.personnel_identity, " +
            "e.recruitment_source, e.entry_mode, e.employment_status, e.ethnicity, e.native_province, e.native_city, " +
            "e.bank_card, e.home_information, e.school_major, e.professional_category, " +
            "e.name_abbreviation, e.create_time, " +
            "p.name AS name, p.id_card AS id_card, p.sex AS sex, p.birthday AS birthday, " +
            "p.remark AS remark, " +
            "d.name AS deptName, sp.name AS postName " +
            "FROM hr_employee e " +
            "LEFT JOIN partner p ON p.id = e.partner_id AND p.deleted = 0 " +
            "LEFT JOIN system_dept d ON d.id = e.dept AND d.deleted = 0 " +
            "LEFT JOIN system_post sp ON sp.id = e.post_id AND sp.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "e.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND e.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.employeeMobile != null and reqVO.employeeMobile != \"\"'>AND e.employee_mobile LIKE CONCAT('%', #{reqVO.employeeMobile}, '%')</if>" +
            "<if test='reqVO.idCard != null and reqVO.idCard != \"\"'>AND p.id_card LIKE CONCAT('%', #{reqVO.idCard}, '%')</if>" +
            "<if test='reqVO.employeeNo != null and reqVO.employeeNo != \"\"'>AND e.employee_no = #{reqVO.employeeNo}</if>" +
            "<if test='reqVO.dept != null'>AND e.dept = #{reqVO.dept}</if>" +
            "<if test='reqVO.postId != null'>AND e.post_id = #{reqVO.postId}</if>" +
            "<if test='reqVO.personnelCategory != null and reqVO.personnelCategory != \"\"'>AND e.personnel_category = #{reqVO.personnelCategory}</if>" +
            "<if test='reqVO.professionalTitle != null and reqVO.professionalTitle != \"\"'>AND e.professional_title = #{reqVO.professionalTitle}</if>" +
            "<if test='reqVO.politicalStatus != null and reqVO.politicalStatus != \"\"'>AND e.political_status = #{reqVO.politicalStatus}</if>" +
            "<if test='reqVO.partyJoinDate != null and reqVO.partyJoinDate.length == 2'>AND e.party_join_date BETWEEN #{reqVO.partyJoinDate[0]} AND #{reqVO.partyJoinDate[1]}</if>" +
            "<if test='reqVO.careerStartDate != null and reqVO.careerStartDate.length == 2'>AND e.career_start_date BETWEEN #{reqVO.careerStartDate[0]} AND #{reqVO.careerStartDate[1]}</if>" +
            "<if test='reqVO.hireDate != null and reqVO.hireDate.length == 2'>AND e.hire_date BETWEEN #{reqVO.hireDate[0]} AND #{reqVO.hireDate[1]}</if>" +
            "<if test='reqVO.position != null and reqVO.position != \"\"'>AND e.position = #{reqVO.position}</if>" +
            "<if test='reqVO.duty != null and reqVO.duty != \"\"'>AND e.duty = #{reqVO.duty}</if>" +
            "<if test='reqVO.appointmentDate != null and reqVO.appointmentDate.length == 2'>AND e.appointment_date BETWEEN #{reqVO.appointmentDate[0]} AND #{reqVO.appointmentDate[1]}</if>" +
            "<if test='reqVO.highestEducation != null and reqVO.highestEducation != \"\"'>AND e.highest_education = #{reqVO.highestEducation}</if>" +
            "<if test='reqVO.fullTimeEducation != null and reqVO.fullTimeEducation != \"\"'>AND e.full_time_education = #{reqVO.fullTimeEducation}</if>" +
            "<if test='reqVO.establishmentStatus != null and reqVO.establishmentStatus != \"\"'>AND e.establishment_status = #{reqVO.establishmentStatus}</if>" +
            "<if test='reqVO.personnelIdentity != null and reqVO.personnelIdentity != \"\"'>AND e.personnel_identity = #{reqVO.personnelIdentity}</if>" +
            "<if test='reqVO.recruitmentSource != null and reqVO.recruitmentSource != \"\"'>AND e.recruitment_source = #{reqVO.recruitmentSource}</if>" +
            "<if test='reqVO.entryMode != null and reqVO.entryMode != \"\"'>AND e.entry_mode = #{reqVO.entryMode}</if>" +
            "<if test='reqVO.employmentStatus != null and reqVO.employmentStatus != \"\"'>AND e.employment_status = #{reqVO.employmentStatus}</if>" +
            "<if test='reqVO.ethnicity != null and reqVO.ethnicity != \"\"'>AND e.ethnicity = #{reqVO.ethnicity}</if>" +
            "<if test='reqVO.nativeProvince != null and reqVO.nativeProvince != \"\"'>AND e.native_province = #{reqVO.nativeProvince}</if>" +
            "<if test='reqVO.nativeCity != null and reqVO.nativeCity != \"\"'>AND e.native_city = #{reqVO.nativeCity}</if>" +
            "<if test='reqVO.bankCard != null and reqVO.bankCard != \"\"'>AND e.bank_card = #{reqVO.bankCard}</if>" +
            "<if test='reqVO.homeInformation != null and reqVO.homeInformation != \"\"'>AND e.home_information = #{reqVO.homeInformation}</if>" +
            "<if test='reqVO.schoolMajor != null and reqVO.schoolMajor != \"\"'>AND e.school_major = #{reqVO.schoolMajor}</if>" +
            "<if test='reqVO.professionalCategory != null and reqVO.professionalCategory != \"\"'>AND e.professional_category = #{reqVO.professionalCategory}</if>" +
            "<if test='reqVO.nameAbbreviation != null and reqVO.nameAbbreviation != \"\"'>AND e.name_abbreviation = #{reqVO.nameAbbreviation}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND e.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY e.id DESC</script>")
    IPage<EmployeeRespVO> selectPageJoin(IPage<EmployeeRespVO> page, @Param("reqVO") EmployeePageReqVO reqVO);

    default PageResult<EmployeeRespVO> selectPage(EmployeePageReqVO reqVO) {
        IPage<EmployeeRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<EmployeeRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select(SELECT_JOIN_COLUMNS + "WHERE e.deleted = 0 AND e.id = #{id}")
    EmployeeRespVO selectByIdJoin(@Param("id") Long id);

    @Select(SELECT_JOIN_COLUMNS + "WHERE e.deleted = 0 AND e.partner_id = #{partnerId} LIMIT 1")
    EmployeeRespVO selectByPartnerIdJoin(@Param("partnerId") Long partnerId);

    @Select("SELECT dept FROM hr_employee WHERE partner_id = #{partnerId} AND deleted = 0 LIMIT 1")
    Long selectDeptIdByPartnerId(@Param("partnerId") Long partnerId);

    @Select("SELECT id FROM hr_employee WHERE partner_id = #{partnerId} AND deleted = 0 LIMIT 1")
    Long selectIdByPartnerId(@Param("partnerId") Long partnerId);

    @Select("SELECT id, partner_id, employee_no, dept, post_id, position FROM hr_employee WHERE partner_id = #{partnerId} AND deleted = 0 LIMIT 1")
    EmployeeDO selectProfileByPartnerId(@Param("partnerId") Long partnerId);

}
