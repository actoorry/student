package vip.appap.suxin.module.hr.controller.admin.salary;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.math.BigDecimal;
import java.util.*;
import java.io.IOException;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.framework.excel.core.util.ExcelUtils;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.*;

import vip.appap.suxin.module.hr.controller.admin.salary.vo.*;
import vip.appap.suxin.module.hr.service.salary.SalaryService;

@Tag(name = "管理后台 - 薪资")
@RestController
@RequestMapping("/hr/salary")
@Validated
public class SalaryController {

    @Resource
    private SalaryService salaryService;

    @PostMapping("/create")
    @Operation(summary = "创建薪资")
    @PreAuthorize("@ss.hasPermission('hr:salary:create')")
    public CommonResult<Long> createSalary(@Valid @RequestBody SalarySaveReqVO createReqVO) {
        return success(salaryService.createSalary(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新薪资")
    @PreAuthorize("@ss.hasPermission('hr:salary:update')")
    public CommonResult<Boolean> updateSalary(@Valid @RequestBody SalarySaveReqVO updateReqVO) {
        salaryService.updateSalary(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除薪资")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:salary:delete')")
    public CommonResult<Boolean> deleteSalary(@RequestParam("id") Long id) {
        salaryService.deleteSalary(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除薪资")
    @PreAuthorize("@ss.hasPermission('hr:salary:delete')")
    public CommonResult<Boolean> deleteSalaryList(@RequestParam("ids") List<Long> ids) {
        salaryService.deleteSalaryListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得薪资")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:salary:query')")
    public CommonResult<SalaryRespVO> getSalary(@RequestParam("id") Long id) {
        return success(salaryService.getSalary(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得薪资分页")
    @PreAuthorize("@ss.hasPermission('hr:salary:query')")
    public CommonResult<PageResult<SalaryRespVO>> getSalaryPage(@Valid SalaryPageReqVO pageReqVO) {
        return success(salaryService.getSalaryPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出薪资 Excel")
    @PreAuthorize("@ss.hasPermission('hr:salary:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportSalaryExcel(@Valid SalaryPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<SalaryRespVO> list = salaryService.getSalaryPage(pageReqVO).getList();
        ExcelUtils.write(response, "薪资.xls", "数据", SalaryRespVO.class, list);
    }

    @PostMapping("/check-import")
    @Operation(summary = "薪资导入预检：解析文件名年月、校验表头、查重复")
    @Parameter(name = "file", description = "Excel 文件", required = true)
    @PreAuthorize("@ss.hasPermission('hr:salary:import')")
    public CommonResult<SalaryImportCheckRespVO> checkSalaryImport(@RequestParam("file") MultipartFile file) throws Exception {
        return success(salaryService.checkSalaryImport(file));
    }

    @PostMapping("/import")
    @Operation(summary = "导入薪资 Excel")
    @Parameters({
            @Parameter(name = "file", description = "Excel 文件", required = true),
            @Parameter(name = "confirmOverwrite", description = "是否确认覆盖已存在的同年月同工号记录", example = "false")
    })
    @PreAuthorize("@ss.hasPermission('hr:salary:import')")
    public CommonResult<SalaryImportRespVO> importSalary(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "confirmOverwrite", required = false, defaultValue = "false") Boolean confirmOverwrite) throws Exception {
        return success(salaryService.importSalary(file, confirmOverwrite));
    }

    @GetMapping("/get-import-template")
    @Operation(summary = "获得薪资导入模板")
    @PreAuthorize("@ss.hasPermission('hr:salary:import')")
    public void importTemplate(HttpServletResponse response) throws IOException {
        List<SalaryImportExcelVO> list = Collections.singletonList(
                SalaryImportExcelVO.builder()
                        .employeeNo("Z0000672")
                        .name("张三")
                        .department("内科")
                        .personnelCategory("管理人员")
                        .basicSalary(new BigDecimal("1765"))
                        .salaryGrade(new BigDecimal("2075"))
                        .unitAllowance(new BigDecimal("80"))
                        .postAllowance(BigDecimal.ZERO)
                        .onlyChildAllowance(BigDecimal.ZERO)
                        .huiEthnicAllowance(BigDecimal.ZERO)
                        .familyPlanningAllowance(BigDecimal.ZERO)
                        .welfareFee(new BigDecimal("3.5"))
                        .officialTransportAllowance(BigDecimal.ZERO)
                        .rehireFee(BigDecimal.ZERO)
                        .backPay(new BigDecimal("300"))
                        .otherWage(BigDecimal.ZERO)
                        .rentAllowance(new BigDecimal("262"))
                        .basicPerformance(new BigDecimal("965"))
                        .performanceSalary(new BigDecimal("2740"))
                        .grossSalaryTotal(new BigDecimal("8190.5"))
                        .socialSecurity(new BigDecimal("553.12"))
                        .medicalInsurance(new BigDecimal("86.22"))
                        .occupationalAnnuity(new BigDecimal("276.56"))
                        .rentFee(BigDecimal.ZERO)
                        .sickLeaveDeduction(BigDecimal.ZERO)
                        .incomeTax(BigDecimal.ZERO)
                        .otherDeduction(BigDecimal.ZERO)
                        .unemploymentInsurance(new BigDecimal("21.56"))
                        .housingFund(new BigDecimal("651"))
                        .unionFee(new BigDecimal("10"))
                        .totalDeduction(new BigDecimal("1598.46"))
                        .netSalaryTotal(new BigDecimal("6592.04"))
                        .taxBase(new BigDecimal("8177"))
                        .childEducation(BigDecimal.ZERO)
                        .continuingEducation(BigDecimal.ZERO)
                        .housingLoanInterest(BigDecimal.ZERO)
                        .housingRent(BigDecimal.ZERO)
                        .elderlySupport(BigDecimal.ZERO)
                        .otherLegalDeduction(BigDecimal.ZERO)
                        .build()
        );
        ExcelUtils.write(response, "薪资导入模板.xls", "薪资", SalaryImportExcelVO.class, list);
    }

}
