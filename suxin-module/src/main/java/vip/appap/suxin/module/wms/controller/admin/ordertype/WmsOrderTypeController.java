package vip.appap.suxin.module.wms.controller.admin.ordertype;

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

import vip.appap.suxin.module.wms.controller.admin.ordertype.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.ordertype.WmsOrderTypeDO;
import vip.appap.suxin.module.wms.service.ordertype.WmsOrderTypeService;

@Tag(name = "管理后台 - 出入库单据类型配置")
@RestController
@RequestMapping("/wms/order-type")
@Validated
public class WmsOrderTypeController {

    @Resource
    private WmsOrderTypeService orderTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建出入库单据类型配置")
    @PreAuthorize("@ss.hasPermission('wms:order-type:create')")
    public CommonResult<Long> createOrderType(@Valid @RequestBody WmsOrderTypeSaveReqVO createReqVO) {
        return success(orderTypeService.createOrderType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出入库单据类型配置")
    @PreAuthorize("@ss.hasPermission('wms:order-type:update')")
    public CommonResult<Boolean> updateOrderType(@Valid @RequestBody WmsOrderTypeSaveReqVO updateReqVO) {
        orderTypeService.updateOrderType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出入库单据类型配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:order-type:delete')")
    public CommonResult<Boolean> deleteOrderType(@RequestParam("id") Long id) {
        orderTypeService.deleteOrderType(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除出入库单据类型配置")
                @PreAuthorize("@ss.hasPermission('wms:order-type:delete')")
    public CommonResult<Boolean> deleteOrderTypeList(@RequestParam("ids") List<Long> ids) {
        orderTypeService.deleteOrderTypeListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出入库单据类型配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:order-type:query')")
    public CommonResult<WmsOrderTypeRespVO> getOrderType(@RequestParam("id") Long id) {
        WmsOrderTypeDO orderType = orderTypeService.getOrderType(id);
        return success(BeanUtils.toBean(orderType, WmsOrderTypeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出入库单据类型配置分页")
    @PreAuthorize("@ss.hasPermission('wms:order-type:query')")
    public CommonResult<PageResult<WmsOrderTypeRespVO>> getOrderTypePage(@Valid WmsOrderTypePageReqVO pageReqVO) {
        PageResult<WmsOrderTypeDO> pageResult = orderTypeService.getOrderTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsOrderTypeRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出入库单据类型配置 Excel")
    @PreAuthorize("@ss.hasPermission('wms:order-type:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOrderTypeExcel(@Valid WmsOrderTypePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsOrderTypeDO> list = orderTypeService.getOrderTypePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出入库单据类型配置.xls", "数据", WmsOrderTypeRespVO.class,
                        BeanUtils.toBean(list, WmsOrderTypeRespVO.class));
    }

}