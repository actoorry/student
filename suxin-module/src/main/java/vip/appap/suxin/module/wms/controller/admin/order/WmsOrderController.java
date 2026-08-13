package vip.appap.suxin.module.wms.controller.admin.order;

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

import vip.appap.suxin.module.wms.controller.admin.order.vo.*;
import vip.appap.suxin.module.wms.dal.dataobject.order.WmsOrderDO;
import vip.appap.suxin.module.wms.service.order.WmsOrderService;

@Tag(name = "管理后台 - 出入库/调拨单据头")
@RestController
@RequestMapping("/wms/order")
@Validated
public class WmsOrderController {

    @Resource
    private WmsOrderService orderService;

    @PostMapping("/create")
    @Operation(summary = "创建出入库/调拨单据头")
    @PreAuthorize("@ss.hasPermission('wms:order:create')")
    public CommonResult<Long> createOrder(@Valid @RequestBody WmsOrderSaveReqVO createReqVO) {
        return success(orderService.createOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新出入库/调拨单据头")
    @PreAuthorize("@ss.hasPermission('wms:order:update')")
    public CommonResult<Boolean> updateOrder(@Valid @RequestBody WmsOrderSaveReqVO updateReqVO) {
        orderService.updateOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除出入库/调拨单据头")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('wms:order:delete')")
    public CommonResult<Boolean> deleteOrder(@RequestParam("id") Long id) {
        orderService.deleteOrder(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除出入库/调拨单据头")
                @PreAuthorize("@ss.hasPermission('wms:order:delete')")
    public CommonResult<Boolean> deleteOrderList(@RequestParam("ids") List<Long> ids) {
        orderService.deleteOrderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得出入库/调拨单据头")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<WmsOrderRespVO> getOrder(@RequestParam("id") Long id) {
        WmsOrderDO order = orderService.getOrder(id);
        return success(BeanUtils.toBean(order, WmsOrderRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得出入库/调拨单据头分页")
    @PreAuthorize("@ss.hasPermission('wms:order:query')")
    public CommonResult<PageResult<WmsOrderRespVO>> getOrderPage(@Valid WmsOrderPageReqVO pageReqVO) {
        PageResult<WmsOrderDO> pageResult = orderService.getOrderPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, WmsOrderRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出出入库/调拨单据头 Excel")
    @PreAuthorize("@ss.hasPermission('wms:order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOrderExcel(@Valid WmsOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<WmsOrderDO> list = orderService.getOrderPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "出入库/调拨单据头.xls", "数据", WmsOrderRespVO.class,
                        BeanUtils.toBean(list, WmsOrderRespVO.class));
    }

    // ========== 单据状态流转 ==========

    @PutMapping("/submit")
    @Operation(summary = "提交审核（草稿→待审核）")
    @PreAuthorize("@ss.hasPermission('wms:order:finish')")
    public CommonResult<Boolean> submitOrder(@RequestParam("id") Long id) {
        orderService.submitOrder(id);
        return success(true);
    }

    @PutMapping("/approve")
    @Operation(summary = "审核通过（待审核→已审核）")
    @PreAuthorize("@ss.hasPermission('wms:order:finish')")
    public CommonResult<Boolean> approveOrder(@RequestParam("id") Long id) {
        orderService.approveOrder(id);
        return success(true);
    }

    @PutMapping("/finish")
    @Operation(summary = "完成（已审核→已完成）")
    @PreAuthorize("@ss.hasPermission('wms:order:finish')")
    public CommonResult<Boolean> finishOrder(@RequestParam("id") Long id) {
        orderService.finishOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消（草稿/待审核→已取消）")
    @PreAuthorize("@ss.hasPermission('wms:order:finish')")
    public CommonResult<Boolean> cancelOrder(@RequestParam("id") Long id) {
        orderService.cancelOrder(id);
        return success(true);
    }

    @PutMapping("/reverse")
    @Operation(summary = "生成退货单（已审核→生成反向草稿单据）")
    @PreAuthorize("@ss.hasPermission('wms:order:create')")
    public CommonResult<Long> reverseOrder(@RequestParam("id") Long id) {
        return success(orderService.reverseOrder(id));
    }

}