package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesDiyTemplateConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyTemplateDO;
import vip.appap.suxin.module.sales.service.SalesDiyPageService;
import vip.appap.suxin.module.sales.service.SalesDiyTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 装修模板")
@RestController
@RequestMapping("/sales/promotion/diy-template")
@Validated
public class SalesDiyTemplateController {

    @Resource
    private SalesDiyTemplateService diyTemplateService;
    @Resource
    private SalesDiyPageService diyPageService;

    @PostMapping("/create")
    @Operation(summary = "创建装修模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:create')")
    public CommonResult<Long> createDiyTemplate(@Valid @RequestBody SalesDiyTemplateCreateReqVO createReqVO) {
        return success(diyTemplateService.createDiyTemplate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新装修模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:update')")
    public CommonResult<Boolean> updateDiyTemplate(@Valid @RequestBody SalesDiyTemplateUpdateReqVO updateReqVO) {
        diyTemplateService.updateDiyTemplate(updateReqVO);
        return success(true);
    }

    @PutMapping("/use")
    @Operation(summary = "使用装修模板")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:use')")
    public CommonResult<Boolean> useDiyTemplate(@RequestParam("id") Long id) {
        diyTemplateService.useDiyTemplate(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除装修模板")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:delete')")
    public CommonResult<Boolean> deleteDiyTemplate(@RequestParam("id") Long id) {
        diyTemplateService.deleteDiyTemplate(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得装修模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:query')")
    public CommonResult<SalesDiyTemplateRespVO> getDiyTemplate(@RequestParam("id") Long id) {
        SalesDiyTemplateDO diyTemplate = diyTemplateService.getDiyTemplate(id);
        return success(SalesDiyTemplateConvert.INSTANCE.convert(diyTemplate));
    }

    @GetMapping("/page")
    @Operation(summary = "获得装修模板分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:query')")
    public CommonResult<PageResult<SalesDiyTemplateRespVO>> getDiyTemplatePage(@Valid SalesDiyTemplatePageReqVO pageVO) {
        PageResult<SalesDiyTemplateDO> pageResult = diyTemplateService.getDiyTemplatePage(pageVO);
        return success(SalesDiyTemplateConvert.INSTANCE.convertPage(pageResult));
    }

    // TODO @疯狂：这个要不和 getDiyTemplate 合并，然后 SalesDiyTemplateRespVO 里面直接把 SalesDiyPagePropertyRespVO 也加上。减少 VO 好了，管理后台 get 多返回点数据，也问题不大的。目的，还是想尽可能降低大家的理解成本哈；
    @GetMapping("/get-property")
    @Operation(summary = "获得装修模板属性")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:query')")
    public CommonResult<SalesDiyTemplatePropertyRespVO> getDiyTemplateProperty(@RequestParam("id") Long id) {
        SalesDiyTemplateDO diyTemplate = diyTemplateService.getDiyTemplate(id);
        List<SalesDiyPageDO> pages = diyPageService.getDiyPageByTemplateId(id);
        return success(SalesDiyTemplateConvert.INSTANCE.convertPropertyVo(diyTemplate, pages));
    }

    // TODO @疯狂：这个接口，要不和 useDiyTemplate 合并成一个，然后 VO 改成我们新的 VO 规范。不改的字段，就不传递。
    @PutMapping("/update-property")
    @Operation(summary = "更新装修模板属性")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_template:update')")
    public CommonResult<Boolean> updateDiyTemplateProperty(@Valid @RequestBody SalesDiyTemplatePropertyUpdateRequestVO updateReqVO) {
        diyTemplateService.updateDiyTemplateProperty(updateReqVO);
        return success(true);
    }

}
