package vip.appap.suxin.module.wms.controller.admin.stock;

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

import vip.appap.suxin.module.wms.controller.admin.stock.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.stock.WmsStockDO;
import vip.appap.suxin.module.wms.service.stock.WmsStockService;

@Tag(name = "管理后台 - 库存快照")
@RestController
@RequestMapping("/wms/stock")
@Validated
public class WmsStockController {

    @Resource
    private WmsStockService stockService;

    @PostMapping("/create")
    @Operation(summary = "创建库存快照")
    @PreAuthorize("@ss.hasPermission('wms:stock:create')")
    public CommonResult<Long> createStock(@Valid @RequestBody WmsStockSaveReqVO createReqVO) {
        return success(stockService.createStock(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新库存快照")
    @PreAuthorize("@ss.hasPermission('wms:stock:update')")
    public CommonResult<Boolean> updateStock(@Valid @RequestBody WmsStockSaveReqVO updateReqVO) {
        stockService.updateStock(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除库存快照")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:stock:delete')")
    public CommonResult<Boolean> deleteStock(@RequestParam("id") Long id) {
        stockService.deleteStock(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除库存快照")
                @PreAuthorize("@ss.hasPermission('wms:stock:delete')")
    public CommonResult<Boolean> deleteStockList(@RequestParam("ids") List<Long> ids) {
        stockService.deleteStockListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得库存快照")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:stock:query')")
    public CommonResult<WmsStockRespVO> getStock(@RequestParam("id") Long id) {
        WmsStockDO stock = stockService.getStock(id);
        return success(BeanUtils.toBean(stock, WmsStockRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得库存快照分页")
    @PreAuthorize("@ss.hasPermission('wms:stock:query')")
    public CommonResult<PageResult<WmsStockRespVO>> getStockPage(@Valid WmsStockPageReqVO pageReqVO) {
        PageResult<WmsStockDO> pageResult = stockService.getStockPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsStockRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出库存快照 Excel")
    @PreAuthorize("@ss.hasPermission('wms:stock:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportStockExcel(@Valid WmsStockPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsStockDO> list = stockService.getStockPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "库存快照.xls", "数据", WmsStockRespVO.class,
                        BeanUtils.toBean(list, WmsStockRespVO.class));
    }

}