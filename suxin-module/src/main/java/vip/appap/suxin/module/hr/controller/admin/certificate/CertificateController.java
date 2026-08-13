package vip.appap.suxin.module.hr.controller.admin.certificate;

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

import vip.appap.suxin.module.hr.controller.admin.certificate.vo.*;
import vip.appap.suxin.module.hr.service.certificate.CertificateService;

@Tag(name = "管理后台 - 人员证书")
@RestController
@RequestMapping("/hr/certificate")
@Validated
public class CertificateController {

    @Resource
    private CertificateService certificateService;

    @PostMapping("/create")
    @Operation(summary = "创建人员证书")
    @PreAuthorize("@ss.hasPermission('hr:certificate:create')")
    public CommonResult<Long> createCertificate(@Valid @RequestBody CertificateSaveReqVO createReqVO) {
        return success(certificateService.createCertificate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新人员证书")
    @PreAuthorize("@ss.hasPermission('hr:certificate:update')")
    public CommonResult<Boolean> updateCertificate(@Valid @RequestBody CertificateSaveReqVO updateReqVO) {
        certificateService.updateCertificate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人员证书")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:certificate:delete')")
    public CommonResult<Boolean> deleteCertificate(@RequestParam("id") Long id) {
        certificateService.deleteCertificate(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除人员证书")
    @PreAuthorize("@ss.hasPermission('hr:certificate:delete')")
    public CommonResult<Boolean> deleteCertificateList(@RequestParam("ids") List<Long> ids) {
        certificateService.deleteCertificateListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得人员证书")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:certificate:query')")
    public CommonResult<CertificateRespVO> getCertificate(@RequestParam("id") Long id) {
        return success(certificateService.getCertificate(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得人员证书分页")
    @PreAuthorize("@ss.hasPermission('hr:certificate:query')")
    public CommonResult<PageResult<CertificateRespVO>> getCertificatePage(@Valid CertificatePageReqVO pageReqVO) {
        return success(certificateService.getCertificatePage(pageReqVO));
    }

    @GetMapping("/ledger-page")
    @Operation(summary = "督查台账分页（默认只显示风险证书）")
    @PreAuthorize("@ss.hasPermission('hr:certificate:ledger')")
    public CommonResult<PageResult<CertificateRespVO>> getCertificateLedgerPage(@Valid CertificatePageReqVO pageReqVO) {
        return success(certificateService.getCertificateLedgerPage(pageReqVO));
    }

    @GetMapping("/ledger-summary")
    @Operation(summary = "督查台账汇总")
    @PreAuthorize("@ss.hasPermission('hr:certificate:ledger')")
    public CommonResult<CertificateLedgerSummaryRespVO> getCertificateLedgerSummary() {
        return success(certificateService.getCertificateLedgerSummary());
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出人员证书 Excel")
    @PreAuthorize("@ss.hasPermission('hr:certificate:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCertificateExcel(@Valid CertificatePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CertificateRespVO> list = certificateService.getCertificatePage(pageReqVO).getList();
        ExcelUtils.write(response, "人员证书.xls", "数据", CertificateRespVO.class, list);
    }

    @GetMapping("/ledger-export-excel")
    @Operation(summary = "导出督查台账 Excel")
    @PreAuthorize("@ss.hasPermission('hr:certificate:ledger')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCertificateLedgerExcel(@Valid CertificatePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CertificateRespVO> list = certificateService.getCertificateLedgerPage(pageReqVO).getList();
        ExcelUtils.write(response, "证书督查台账.xls", "数据", CertificateRespVO.class, list);
    }

}
