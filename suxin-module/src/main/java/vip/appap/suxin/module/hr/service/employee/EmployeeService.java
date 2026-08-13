package vip.appap.suxin.module.hr.service.employee;

import java.util.List;
import jakarta.validation.*;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.*;
import vip.appap.suxin.framework.common.pojo.PageResult;

/**
 * 员工 Service 接口
 *
 * @author admin
 */
public interface EmployeeService {

    /**
     * 创建员工
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createEmployee(@Valid EmployeeSaveReqVO createReqVO);

    /**
     * 更新员工
     *
     * @param updateReqVO 更新信息
     */
    void updateEmployee(@Valid EmployeeSaveReqVO updateReqVO);

    /**
     * 删除员工
     *
     * @param id 编号
     */
    void deleteEmployee(Long id);

    /**
    * 批量删除员工
    *
    * @param ids 编号
    */
    void deleteEmployeeListByIds(List<Long> ids);

    /**
     * 获得员工
     *
     * @param id 编号
     * @return 员工
     */
    EmployeeRespVO getEmployee(Long id);

    /**
     * 获得员工分页
     *
     * @param pageReqVO 分页查询
     * @return 员工分页
     */
    PageResult<EmployeeRespVO> getEmployeePage(EmployeePageReqVO pageReqVO);

}