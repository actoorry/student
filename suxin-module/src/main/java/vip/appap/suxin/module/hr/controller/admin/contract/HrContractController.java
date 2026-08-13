package vip.appap.suxin.module.hr.controller.admin.contract;

import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

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

import vip.appap.suxin.module.hr.controller.admin.contract.vo.*;
import vip.appap.suxin.module.hr.service.contract.HrContractService;

@Tag(name = "管理后台 - 劳动合同")
@RestController
@RequestMapping("/hr/contract")
@Validated
public class HrContractController {

    @Resource
    private HrContractService contractService;

    @PostMapping("/create")
    @Operation(summary = "创建劳动合同")
    @PreAuthorize("@ss.hasPermission('hr:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody HrContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新劳动合同")
    @PreAuthorize("@ss.hasPermission('hr:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody HrContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除劳动合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除劳动合同")
    @PreAuthorize("@ss.hasPermission('hr:contract:delete')")
    public CommonResult<Boolean> deleteContractList(@RequestParam("ids") List<Long> ids) {
        contractService.deleteContractListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得劳动合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:contract:query')")
    public CommonResult<HrContractRespVO> getContract(@RequestParam("id") Long id) {
        return success(contractService.getContract(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得劳动合同分页")
    @PreAuthorize("@ss.hasPermission('hr:contract:query')")
    public CommonResult<PageResult<HrContractRespVO>> getContractPage(@Valid HrContractPageReqVO pageReqVO) {
        return success(contractService.getContractPage(pageReqVO));
    }

    @GetMapping("/preview-no")
    @Operation(summary = "预览合同编号")
    @PreAuthorize("@ss.hasPermission('hr:contract:create')")
    public CommonResult<String> previewContractNo() {
        return success(contractService.previewContractNo());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出劳动合同 Excel")
    @PreAuthorize("@ss.hasPermission('hr:contract:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContractExcel(@Valid HrContractPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<HrContractRespVO> list = contractService.getContractPage(pageReqVO).getList();
        ExcelUtils.write(response, "劳动合同.xls", "数据", HrContractRespVO.class, list);
    }

}
