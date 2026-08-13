package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesArticleCategoryConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleCategoryDO;
import vip.appap.suxin.module.sales.service.SalesArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文章分类")
@RestController
@RequestMapping("/sales/promotion/article-category")
@Validated
public class SalesArticleCategoryController {

    @Resource
    private SalesArticleCategoryService articleCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建文章分类")
    @PreAuthorize("@ss.hasPermission('sales:sales_article_category:create')")
    public CommonResult<Long> createArticleCategory(@Valid @RequestBody SalesArticleCategoryCreateReqVO createReqVO) {
        return success(articleCategoryService.createArticleCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章分类")
    @PreAuthorize("@ss.hasPermission('sales:sales_article_category:update')")
    public CommonResult<Boolean> updateArticleCategory(@Valid @RequestBody SalesArticleCategoryUpdateReqVO updateReqVO) {
        articleCategoryService.updateArticleCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_article_category:delete')")
    public CommonResult<Boolean> deleteArticleCategory(@RequestParam("id") Long id) {
        articleCategoryService.deleteArticleCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_article_category:query')")
    public CommonResult<SalesArticleCategoryRespVO> getArticleCategory(@RequestParam("id") Long id) {
        SalesArticleCategoryDO category = articleCategoryService.getArticleCategory(id);
        return success(SalesArticleCategoryConvert.INSTANCE.convert(category));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取文章分类精简信息列表", description = "只包含被开启的文章分类，主要用于前端的下拉选项")
    public CommonResult<List<SalesArticleCategorySimpleRespVO>> getSimpleDeptList() {
        // 获得分类列表，只要开启状态的
        List<SalesArticleCategoryDO> list = articleCategoryService.getArticleCategoryListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // 降序排序后，返回给前端
        list.sort(Comparator.comparing(SalesArticleCategoryDO::getSort).reversed());
        return success(SalesArticleCategoryConvert.INSTANCE.convertList03(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分类分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_article_category:query')")
    public CommonResult<PageResult<SalesArticleCategoryRespVO>> getArticleCategoryPage(@Valid SalesArticleCategoryPageReqVO pageVO) {
        PageResult<SalesArticleCategoryDO> pageResult = articleCategoryService.getArticleCategoryPage(pageVO);
        return success(SalesArticleCategoryConvert.INSTANCE.convertPage(pageResult));
    }

}
