package vip.appap.suxin.module.wms.controller.admin.orderitem;

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

import vip.appap.suxin.module.wms.controller.admin.orderitem.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.orderitem.WmsOrderItemDO;
import vip.appap.suxin.module.wms.service.orderitem.WmsOrderItemService;

@Tag(name = "管理后台 - 出入库/调拨单据明细")
@RestController
@RequestMapping("/wms/order-item")
@Validated
public class WmsOrderItemController {

    @Resource
    private WmsOrderItemService orderItemService;

    @PostMapping("/create")
    @Operation(summary = "创建出入库/调拨单据明细")
    @PreAuthorize("@ss.hasPermission('wms:order-item:create')")
    public CommonResult<Long> createOrderItem(@Valid @RequestBody WmsOrderItemSaveReqVO createReqVO) {
        return success(orderItemService.createOrderItem(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出入库/调拨单据明细")
    @PreAuthorize("@ss.hasPermission('wms:order-item:update')")
    public CommonResult<Boolean> updateOrderItem(@Valid @RequestBody WmsOrderItemSaveReqVO updateReqVO) {
        orderItemService.updateOrderItem(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出入库/调拨单据明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:order-item:delete')")
    public CommonResult<Boolean> deleteOrderItem(@RequestParam("id") Long id) {
        orderItemService.deleteOrderItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除出入库/调拨单据明细")
                @PreAuthorize("@ss.hasPermission('wms:order-item:delete')")
    public CommonResult<Boolean> deleteOrderItemList(@RequestParam("ids") List<Long> ids) {
        orderItemService.deleteOrderItemListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出入库/调拨单据明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:order-item:query')")
    public CommonResult<WmsOrderItemRespVO> getOrderItem(@RequestParam("id") Long id) {
        WmsOrderItemDO orderItem = orderItemService.getOrderItem(id);
        return success(BeanUtils.toBean(orderItem, WmsOrderItemRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出入库/调拨单据明细分页")
    @PreAuthorize("@ss.hasPermission('wms:order-item:query')")
    public CommonResult<PageResult<WmsOrderItemRespVO>> getOrderItemPage(@Valid WmsOrderItemPageReqVO pageReqVO) {
        PageResult<WmsOrderItemDO> pageResult = orderItemService.getOrderItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsOrderItemRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出入库/调拨单据明细 Excel")
    @PreAuthorize("@ss.hasPermission('wms:order-item:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOrderItemExcel(@Valid WmsOrderItemPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsOrderItemDO> list = orderItemService.getOrderItemPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出入库/调拨单据明细.xls", "数据", WmsOrderItemRespVO.class,
                        BeanUtils.toBean(list, WmsOrderItemRespVO.class));
    }

}