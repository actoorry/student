package vip.appap.suxin.module.hr.controller.admin.overtime;

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

import vip.appap.suxin.module.hr.controller.admin.overtime.vo.*;
import vip.appap.suxin.module.hr.service.overtime.OvertimeService;

@Tag(name = "管理后台 - 加班登记")
@RestController
@RequestMapping("/hr/overtime")
@Validated
public class OvertimeController {

    @Resource
    private OvertimeService overtimeService;

    @PostMapping("/create")
    @Operation(summary = "创建加班登记")
    @PreAuthorize("@ss.hasPermission('hr:overtime:create')")
    public CommonResult<Long> createOvertime(@Valid @RequestBody OvertimeSaveReqVO createReqVO) {
        return success(overtimeService.createOvertime(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新加班登记")
    @PreAuthorize("@ss.hasPermission('hr:overtime:update')")
    public CommonResult<Boolean> updateOvertime(@Valid @RequestBody OvertimeSaveReqVO updateReqVO) {
        overtimeService.updateOvertime(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除加班登记")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:overtime:delete')")
    public CommonResult<Boolean> deleteOvertime(@RequestParam("id") Long id) {
        overtimeService.deleteOvertime(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除加班登记")
    @PreAuthorize("@ss.hasPermission('hr:overtime:delete')")
    public CommonResult<Boolean> deleteOvertimeList(@RequestParam("ids") List<Long> ids) {
        overtimeService.deleteOvertimeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得加班登记")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:overtime:query')")
    public CommonResult<OvertimeRespVO> getOvertime(@RequestParam("id") Long id) {
        return success(overtimeService.getOvertime(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得加班登记分页")
    @PreAuthorize("@ss.hasPermission('hr:overtime:query')")
    public CommonResult<PageResult<OvertimeRespVO>> getOvertimePage(@Valid OvertimePageReqVO pageReqVO) {
        return success(overtimeService.getOvertimePage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出加班登记 Excel")
    @PreAuthorize("@ss.hasPermission('hr:overtime:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOvertimeExcel(@Valid OvertimePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OvertimeRespVO> list = overtimeService.getOvertimePage(pageReqVO).getList();
        ExcelUtils.write(response, "加班登记.xls", "数据", OvertimeRespVO.class, list);
    }

}
