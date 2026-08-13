package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesDiyPageConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesDiyPageDO;
import vip.appap.suxin.module.sales.service.SalesDiyPageService;
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

@Tag(name = "管理后台 - 装修页面")
@RestController
@RequestMapping("/sales/promotion/diy-page")
@Validated
public class SalesDiyPageController {

    @Resource
    private SalesDiyPageService diyPageService;

    @PostMapping("/create")
    @Operation(summary = "创建装修页面")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:create')")
    public CommonResult<Long> createDiyPage(@Valid @RequestBody SalesDiyPageCreateReqVO createReqVO) {
        return success(diyPageService.createDiyPage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新装修页面")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:update')")
    public CommonResult<Boolean> updateDiyPage(@Valid @RequestBody SalesDiyPageUpdateReqVO updateReqVO) {
        diyPageService.updateDiyPage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除装修页面")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:delete')")
    public CommonResult<Boolean> deleteDiyPage(@RequestParam("id") Long id) {
        diyPageService.deleteDiyPage(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得装修页面")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:query')")
    public CommonResult<SalesDiyPageRespVO> getDiyPage(@RequestParam("id") Long id) {
        SalesDiyPageDO diyPage = diyPageService.getDiyPage(id);
        return success(SalesDiyPageConvert.INSTANCE.convert(diyPage));
    }

    @GetMapping("/list")
    @Operation(summary = "获得装修页面列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:query')")
    public CommonResult<List<SalesDiyPageRespVO>> getDiyPageList(@RequestParam("ids") Collection<Long> ids) {
        List<SalesDiyPageDO> list = diyPageService.getDiyPageList(ids);
        return success(SalesDiyPageConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得装修页面分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:query')")
    public CommonResult<PageResult<SalesDiyPageRespVO>> getDiyPagePage(@Valid SalesDiyPagePageReqVO pageVO) {
        PageResult<SalesDiyPageDO> pageResult = diyPageService.getDiyPagePage(pageVO);
        return success(SalesDiyPageConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get-property")
    @Operation(summary = "获得装修页面属性")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:query')")
    public CommonResult<SalesDiyPagePropertyRespVO> getDiyPageProperty(@RequestParam("id") Long id) {
        SalesDiyPageDO diyPage = diyPageService.getDiyPage(id);
        return success(SalesDiyPageConvert.INSTANCE.convertPropertyVo(diyPage));
    }

    @PutMapping("/update-property")
    @Operation(summary = "更新装修页面属性")
    @PreAuthorize("@ss.hasPermission('sales:sales_diy_page:update')")
    public CommonResult<Boolean> updateDiyPageProperty(@Valid @RequestBody SalesDiyPagePropertyUpdateRequestVO updateReqVO) {
        diyPageService.updateDiyPageProperty(updateReqVO);
        return success(true);
    }

}
