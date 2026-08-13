package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesDeliveryExpressConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressDO;
import vip.appap.suxin.module.sales.service.SalesDeliveryExpressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 快递公司")
@RestController
@RequestMapping("/sales/delivery/express")
@Validated
public class SalesDeliveryExpressController {

    @Resource
    private SalesDeliveryExpressService deliveryExpressService;

    @PostMapping("/create")
    @Operation(summary = "创建快递公司")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express:create')")
    public CommonResult<Long> createDeliveryExpress(@Valid @RequestBody SalesDeliveryExpressCreateReqVO createReqVO) {
        return success(deliveryExpressService.createDeliveryExpress(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新快递公司")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express:update')")
    public CommonResult<Boolean> updateDeliveryExpress(@Valid @RequestBody SalesDeliveryExpressUpdateReqVO updateReqVO) {
        deliveryExpressService.updateDeliveryExpress(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除快递公司")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express:delete')")
    public CommonResult<Boolean> deleteDeliveryExpress(@RequestParam("id") Long id) {
        deliveryExpressService.deleteDeliveryExpress(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得快递公司")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express:query')")
    public CommonResult<SalesDeliveryExpressRespVO> getDeliveryExpress(@RequestParam("id") Long id) {
        SalesDeliveryExpressDO deliveryExpress = deliveryExpressService.getDeliveryExpress(id);
        return success(SalesDeliveryExpressConvert.INSTANCE.convert(deliveryExpress));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取快递公司精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<SalesDeliveryExpressSimpleRespVO>> getSimpleDeliveryExpressList() {
        List<SalesDeliveryExpressDO> list = deliveryExpressService.getDeliveryExpressListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(SalesDeliveryExpressConvert.INSTANCE.convertList1(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得快递公司分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express:query')")
    public CommonResult<PageResult<SalesDeliveryExpressRespVO>> getDeliveryExpressPage(@Valid SalesDeliveryExpressPageReqVO pageVO) {
        PageResult<SalesDeliveryExpressDO> pageResult = deliveryExpressService.getDeliveryExpressPage(pageVO);
        return success(SalesDeliveryExpressConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出快递公司 Excel")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportDeliveryExpressExcel(@Valid SalesDeliveryExpressExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SalesDeliveryExpressDO> list = deliveryExpressService.getDeliveryExpressList(exportReqVO);
        // 导出 Excel
        List<SalesDeliveryExpressExcelVO> dataList = SalesDeliveryExpressConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "快递公司.xls", "数据", SalesDeliveryExpressExcelVO.class, dataList);
    }
}
