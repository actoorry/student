package vip.appap.suxin.module.hr.controller.admin.resume;

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

import vip.appap.suxin.module.hr.controller.admin.resume.vo.*;
import vip.appap.suxin.module.hr.service.resume.ResumeService;

@Tag(name = "管理后台 - 履历")
@RestController
@RequestMapping("/hr/resume")
@Validated
public class ResumeController {

    @Resource
    private ResumeService resumeService;

    @PostMapping("/create")
    @Operation(summary = "创建履历")
    @PreAuthorize("@ss.hasPermission('hr:resume:create')")
    public CommonResult<Long> createResume(@Valid @RequestBody ResumeSaveReqVO createReqVO) {
        return success(resumeService.createResume(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新履历")
    @PreAuthorize("@ss.hasPermission('hr:resume:update')")
    public CommonResult<Boolean> updateResume(@Valid @RequestBody ResumeSaveReqVO updateReqVO) {
        resumeService.updateResume(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除履历")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:resume:delete')")
    public CommonResult<Boolean> deleteResume(@RequestParam("id") Long id) {
        resumeService.deleteResume(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除履历")
    @PreAuthorize("@ss.hasPermission('hr:resume:delete')")
    public CommonResult<Boolean> deleteResumeList(@RequestParam("ids") List<Long> ids) {
        resumeService.deleteResumeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得履历")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:resume:query')")
    public CommonResult<ResumeRespVO> getResume(@RequestParam("id") Long id) {
        return success(resumeService.getResume(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得履历分页")
    @PreAuthorize("@ss.hasPermission('hr:resume:query')")
    public CommonResult<PageResult<ResumeRespVO>> getResumePage(@Valid ResumePageReqVO pageReqVO) {
        return success(resumeService.getResumePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出履历 Excel")
    @PreAuthorize("@ss.hasPermission('hr:resume:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportResumeExcel(@Valid ResumePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ResumeRespVO> list = resumeService.getResumePage(pageReqVO).getList();
        ExcelUtils.write(response, "履历.xls", "数据", ResumeRespVO.class, list);
    }

}
