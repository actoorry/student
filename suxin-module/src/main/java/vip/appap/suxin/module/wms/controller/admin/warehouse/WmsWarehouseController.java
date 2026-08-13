package vip.appap.suxin.module.wms.controller.admin.warehouse;

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

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

import vip.appap.suxin.framework.excel.core.util.ExcelUtils;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.*;

import vip.appap.suxin.module.wms.controller.admin.warehouse.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import vip.appap.suxin.module.wms.service.warehouse.WmsWarehouseService;
import vip.appap.suxin.framework.ip.core.utils.AreaUtils;

@Tag(name = "管理后台 - 仓库位置表（仓库/库区/库位树形结构）")
@RestController
@RequestMapping("/wms/warehouse")
@Validated
public class WmsWarehouseController {

    @Resource
    private WmsWarehouseService warehouseService;

    @PostMapping("/create")
    @Operation(summary = "创建仓库位置表（仓库/库区/库位树形结构）")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:create')")
    public CommonResult<Long> createWarehouse(@Valid @RequestBody WmsWarehouseSaveReqVO createReqVO) {
        return success(warehouseService.createWarehouse(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新仓库位置表（仓库/库区/库位树形结构）")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:update')")
    public CommonResult<Boolean> updateWarehouse(@Valid @RequestBody WmsWarehouseSaveReqVO updateReqVO) {
        warehouseService.updateWarehouse(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除仓库位置表（仓库/库区/库位树形结构）")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:warehouse:delete')")
    public CommonResult<Boolean> deleteWarehouse(@RequestParam("id") Long id) {
        warehouseService.deleteWarehouse(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除仓库位置表（仓库/库区/库位树形结构）")
                @PreAuthorize("@ss.hasPermission('wms:warehouse:delete')")
    public CommonResult<Boolean> deleteWarehouseList(@RequestParam("ids") List<Long> ids) {
        warehouseService.deleteWarehouseListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得仓库位置表（仓库/库区/库位树形结构）")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<WmsWarehouseRespVO> getWarehouse(@RequestParam("id") Long id) {
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(id);
        WmsWarehouseRespVO respVO = BeanUtils.toBean(warehouse, WmsWarehouseRespVO.class);
        if (respVO != null && respVO.getAreaId() != null) {
            respVO.setAreaName(AreaUtils.format(respVO.getAreaId()));
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得仓库位置表（仓库/库区/库位树形结构）分页")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:query')")
    public CommonResult<PageResult<WmsWarehouseRespVO>> getWarehousePage(@Valid WmsWarehousePageReqVO pageReqVO) {
        PageResult<WmsWarehouseDO> pageResult = warehouseService.getWarehousePage(pageReqVO);
        PageResult<WmsWarehouseRespVO> voPageResult = BeanUtils.toBean(pageResult, WmsWarehouseRespVO.class);
        // 填充 areaName
        if (voPageResult != null && CollUtil.isNotEmpty(voPageResult.getList())) {
            for (WmsWarehouseRespVO vo : voPageResult.getList()) {
                if (vo.getAreaId() != null) {
                    vo.setAreaName(AreaUtils.format(vo.getAreaId()));
                }
            }
        }
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出仓库位置表（仓库/库区/库位树形结构） Excel")
    @PreAuthorize("@ss.hasPermission('wms:warehouse:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWarehouseExcel(@Valid WmsWarehousePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsWarehouseDO> list = warehouseService.getWarehousePage(pageReqVO).getList();
        List<WmsWarehouseRespVO> voList = BeanUtils.toBean(list, WmsWarehouseRespVO.class);
        // 填充 areaName
        if (CollUtil.isNotEmpty(voList)) {
            for (WmsWarehouseRespVO vo : voList) {
                if (vo.getAreaId() != null) {
                    vo.setAreaName(AreaUtils.format(vo.getAreaId()));
                }
            }
        }
        // 导出 Excel
        ExcelUtils.write(response, "仓库位置表（仓库/库区/库位树形结构）.xls", "数据", WmsWarehouseRespVO.class,
                        voList);
    }

}