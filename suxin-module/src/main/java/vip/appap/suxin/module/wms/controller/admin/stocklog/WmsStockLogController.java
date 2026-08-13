package vip.appap.suxin.module.wms.controller.admin.stocklog;

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
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.framework.excel.core.util.ExcelUtils;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.*;

import vip.appap.suxin.module.wms.controller.admin.stocklog.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.stocklog.WmsStockLogDO;
import vip.appap.suxin.module.wms.service.stocklog.WmsStockLogService;

@Tag(name = "管理后台 - 库存流水台账")
@RestController
@RequestMapping("/wms/stock-log")
@Validated
public class WmsStockLogController {

    @Resource
    private WmsStockLogService stockLogService;

    @PostMapping("/create")
    @Operation(summary = "创建库存流水台账")
    @PreAuthorize("@ss.hasPermission('wms:stock-log:create')")
    public CommonResult<Long> createStockLog(@Valid @RequestBody WmsStockLogSaveReqVO createReqVO) {
        return success(stockLogService.createStockLog(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存流水台账")
    @PreAuthorize("@ss.hasPermission('wms:stock-log:update')")
    public CommonResult<Boolean> updateStockLog(@Valid @RequestBody WmsStockLogSaveReqVO updateReqVO) {
        stockLogService.updateStockLog(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存流水台账")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock-log:delete')")
    public CommonResult<Boolean> deleteStockLog(@RequestParam("id") Long id) {
        stockLogService.deleteStockLog(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除库存流水台账")
                @PreAuthorize("@ss.hasPermission('wms:stock-log:delete')")
    public CommonResult<Boolean> deleteStockLogList(@RequestParam("ids") List<Long> ids) {
        stockLogService.deleteStockLogListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存流水台账")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock-log:query')")
    public CommonResult<WmsStockLogRespVO> getStockLog(@RequestParam("id") Long id) {
        WmsStockLogDO stockLog = stockLogService.getStockLog(id);
        return success(BeanUtils.toBean(stockLog, WmsStockLogRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存流水台账分页")
    @PreAuthorize("@ss.hasPermission('wms:stock-log:query')")
    public CommonResult<PageResult<WmsStockLogRespVO>> getStockLogPage(@Valid WmsStockLogPageReqVO pageReqVO) {
        PageResult<WmsStockLogDO> pageResult = stockLogService.getStockLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存流水台账 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockLogExcel(@Valid WmsStockLogPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockLogDO> list = stockLogService.getStockLogPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存流水台账.xls", "数据", WmsStockLogRespVO.class,
                        BeanUtils.toBean(list, WmsStockLogRespVO.class));
    }

}