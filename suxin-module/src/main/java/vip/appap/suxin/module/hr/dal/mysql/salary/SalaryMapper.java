package vip.appap.suxin.module.hr.dal.mysql.salary;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.framework.mybatis.core.query.LambdaQueryWrapperX;
import vip.appap.suxin.module.hr.controller.admin.salary.vo.SalaryPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.salary.vo.SalaryRespVO;
import vip.appap.suxin.module.hr.dal.dataobject.salary.SalaryDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * 薪资 Mapper
 *
 * @author admin
 */
@Mapper
public interface SalaryMapper extends BaseMapperX<SalaryDO> {

    String SELECT_JOIN_COLUMNS =
            "SELECT s.id, s.partner_id, " +
            "COALESCE(ds.id, e.dept) AS dept, " +
            "s.employee_no, s.personnel_category, " +
            "s.year, s.month, s.basic_salary, s.salary_grade, s.unit_allowance, " +
            "s.post_allowance, s.only_child_allowance, s.hui_ethnic_allowance, " +
            "s.family_planning_allowance, s.welfare_fee, s.official_transport_allowance, " +
            "s.rent_allowance, s.rehire_fee, s.back_pay, s.other_wage, " +
            "s.basic_performance, s.performance_salary, s.gross_salary_total, " +
            "s.social_security, s.medical_insurance, s.occupational_annuity, " +
            "s.unemployment_insurance, s.housing_fund, s.union_fee, s.rent_fee, " +
            "s.sick_leave_deduction, s.income_tax, s.other_deduction, s.total_deduction, " +
            "s.net_salary_total, s.tax_base, s.child_education, s.continuing_education, " +
            "s.housing_loan_interest, s.housing_rent, s.elderly_support, s.other_legal_deduction, " +
            "s.create_time, " +
            "p.name AS name, COALESCE(ds.name, de.name) AS deptName " +
            "FROM hr_salary s " +
            "LEFT JOIN partner p ON p.id = s.partner_id AND p.deleted = 0 " +
            "LEFT JOIN hr_employee e ON e.partner_id = s.partner_id AND e.deleted = 0 " +
            "LEFT JOIN system_dept ds ON ds.id = CAST(NULLIF(s.department, '') AS UNSIGNED) AND ds.deleted = 0 " +
            "LEFT JOIN system_dept de ON de.id = e.dept AND de.deleted = 0 ";

    String WHERE_JOIN_PAGE =
            "<where>" +
            "s.deleted = 0 " +
            "<if test='reqVO.partnerId != null'>AND s.partner_id = #{reqVO.partnerId}</if>" +
            "<if test='reqVO.name != null and reqVO.name != \"\"'>AND p.name LIKE CONCAT('%', #{reqVO.name}, '%')</if>" +
            "<if test='reqVO.dept != null'>AND (ds.id = #{reqVO.dept} OR e.dept = #{reqVO.dept})</if>" +
            "<if test='reqVO.employeeNo != null and reqVO.employeeNo != \"\"'>AND s.employee_no = #{reqVO.employeeNo}</if>" +
            "<if test='reqVO.personnelCategory != null and reqVO.personnelCategory != \"\"'>AND s.personnel_category = #{reqVO.personnelCategory}</if>" +
            "<if test='reqVO.year != null'>AND s.year = #{reqVO.year}</if>" +
            "<if test='reqVO.month != null'>AND s.month = #{reqVO.month}</if>" +
            "<if test='reqVO.createTime != null and reqVO.createTime.length == 2'>AND s.create_time BETWEEN #{reqVO.createTime[0]} AND #{reqVO.createTime[1]}</if>" +
            "</where>";

    @Select("<script>" + SELECT_JOIN_COLUMNS + WHERE_JOIN_PAGE + "ORDER BY s.id DESC</script>")
    IPage<SalaryRespVO> selectPageJoin(IPage<SalaryRespVO> page, @Param("reqVO") SalaryPageReqVO reqVO);

    default PageResult<SalaryRespVO> selectPage(SalaryPageReqVO reqVO) {
        IPage<SalaryRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<SalaryRespVO> result = selectPageJoin(page, reqVO);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Select(SELECT_JOIN_COLUMNS + "WHERE s.deleted = 0 AND s.id = #{id}")
    SalaryRespVO selectByIdJoin(@Param("id") Long id);

    /**
     * 按年份+月份+工号集合查询已有薪资记录（导入重复预检 / upsert 用）
     */
    default List<SalaryDO> selectByYearMonthAndEmployeeNos(Integer year, Integer month, Collection<String> employeeNos) {
        return selectList(new LambdaQueryWrapperX<SalaryDO>()
                .eqIfPresent(SalaryDO::getYear, year)
                .eqIfPresent(SalaryDO::getMonth, month)
                .inIfPresent(SalaryDO::getEmployeeNo, employeeNos));
    }

}
