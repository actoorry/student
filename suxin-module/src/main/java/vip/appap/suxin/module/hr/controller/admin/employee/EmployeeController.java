package vip.appap.suxin.module.hr.controller.admin.employee;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.constraints.*;
import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.framework.excel.core.util.ExcelUtils;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.*;

import vip.appap.suxin.module.hr.controller.admin.employee.vo.*;
import vip.appap.suxin.module.hr.service.employee.EmployeeService;

@Tag(name = "管理后台 - 员工")
@RestController
@RequestMapping("/hr/employee")
@Validated
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @PostMapping("/create")
    @Operation(summary = "创建员工")
    @PreAuthorize("@ss.hasPermission('hr:employee:create')")
    public CommonResult<Long> createEmployee(@Valid @RequestBody EmployeeSaveReqVO createReqVO) {
        return success(employeeService.createEmployee(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新员工")
    @PreAuthorize("@ss.hasPermission('hr:employee:update')")
    public CommonResult<Boolean> updateEmployee(@Valid @RequestBody EmployeeSaveReqVO updateReqVO) {
        employeeService.updateEmployee(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除员工")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:employee:delete')")
    public CommonResult<Boolean> deleteEmployee(@RequestParam("id") Long id) {
        employeeService.deleteEmployee(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除员工")
                @PreAuthorize("@ss.hasPermission('hr:employee:delete')")
    public CommonResult<Boolean> deleteEmployeeList(@RequestParam("ids") List<Long> ids) {
        employeeService.deleteEmployeeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得员工")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:employee:query')")
    public CommonResult<EmployeeRespVO> getEmployee(@RequestParam("id") Long id) {
        return success(employeeService.getEmployee(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得员工分页")
    @PreAuthorize("@ss.hasPermission('hr:employee:query')")
    public CommonResult<PageResult<EmployeeRespVO>> getEmployeePage(@Valid EmployeePageReqVO pageReqVO) {
        return success(employeeService.getEmployeePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出员工 Excel")
    @PreAuthorize("@ss.hasPermission('hr:employee:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportEmployeeExcel(@Valid EmployeePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<EmployeeRespVO> list = employeeService.getEmployeePage(pageReqVO).getList();
        ExcelUtils.write(response, "员工.xls", "数据", EmployeeRespVO.class, list);
    }

}