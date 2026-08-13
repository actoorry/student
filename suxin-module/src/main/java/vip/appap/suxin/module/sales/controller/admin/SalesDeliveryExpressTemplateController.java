package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesDeliveryExpressTemplateConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDeliveryExpressTemplateDO;
import vip.appap.suxin.module.sales.service.SalesDeliveryExpressTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 快递运费模板")
@RestController
@RequestMapping("/sales/delivery/express-template")
@Validated
public class SalesDeliveryExpressTemplateController {

    @Resource
    private SalesDeliveryExpressTemplateService deliveryExpressTemplateService;

    @PostMapping("/create")
    @Operation(summary = "创建快递运费模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express_template:create')")
    public CommonResult<Long> createDeliveryExpressTemplate(@Valid @RequestBody SalesDeliveryExpressTemplateCreateReqVO createReqVO) {
        return success(deliveryExpressTemplateService.createDeliveryExpressTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新快递运费模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express_template:update')")
    public CommonResult<Boolean> updateDeliveryExpressTemplate(@Valid @RequestBody SalesDeliveryExpressTemplateUpdateReqVO updateReqVO) {
        deliveryExpressTemplateService.updateDeliveryExpressTemplate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除快递运费模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express_template:delete')")
    public CommonResult<Boolean> deleteDeliveryExpressTemplate(@RequestParam("id") Long id) {
        deliveryExpressTemplateService.deleteDeliveryExpressTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得快递运费模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express_template:query')")
    public CommonResult<SalesDeliveryExpressTemplateDetailRespVO> getDeliveryExpressTemplate(@RequestParam("id") Long id) {
        return success(deliveryExpressTemplateService.getDeliveryExpressTemplate(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获得快递运费模板列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express_template:query')")
    public CommonResult<List<SalesDeliveryExpressTemplateRespVO>> getDeliveryExpressTemplateList(@RequestParam("ids") Collection<Long> ids) {
        List<SalesDeliveryExpressTemplateDO> list = deliveryExpressTemplateService.getDeliveryExpressTemplateList(ids);
        return success(SalesDeliveryExpressTemplateConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取快递模版精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<SalesDeliveryExpressTemplateSimpleRespVO>> getSimpleTemplateList() {
        // 获取运费模版列表，只要开启状态的
        List<SalesDeliveryExpressTemplateDO> list = deliveryExpressTemplateService.getDeliveryExpressTemplateList();
        // 排序后，返回给前端
        return success(SalesDeliveryExpressTemplateConvert.INSTANCE.convertList1(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得快递运费模板分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_delivery_express_template:query')")
    public CommonResult<PageResult<SalesDeliveryExpressTemplateRespVO>> getDeliveryExpressTemplatePage(@Valid SalesDeliveryExpressTemplatePageReqVO pageVO) {
        PageResult<SalesDeliveryExpressTemplateDO> pageResult = deliveryExpressTemplateService.getDeliveryExpressTemplatePage(pageVO);
        return success(SalesDeliveryExpressTemplateConvert.INSTANCE.convertPage(pageResult));
    }

}
