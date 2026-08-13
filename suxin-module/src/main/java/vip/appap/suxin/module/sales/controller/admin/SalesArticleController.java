package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleCreateReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticlePageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesArticleUpdateReqVO;
import vip.appap.suxin.module.sales.convert.SalesArticleConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleDO;
import vip.appap.suxin.module.sales.service.SalesArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 文章管理")
@RestController
@RequestMapping("/sales/promotion/article")
@Validated
public class SalesArticleController {

    @Resource
    private SalesArticleService articleService;

    @PostMapping("/create")
    @Operation(summary = "创建文章管理")
    @PreAuthorize("@ss.hasPermission('sales:sales_article:create')")
    public CommonResult<Long> createArticle(@Valid @RequestBody SalesArticleCreateReqVO createReqVO) {
        return success(articleService.createArticle(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章管理")
    @PreAuthorize("@ss.hasPermission('sales:sales_article:update')")
    public CommonResult<Boolean> updateArticle(@Valid @RequestBody SalesArticleUpdateReqVO updateReqVO) {
        articleService.updateArticle(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章管理")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_article:delete')")
    public CommonResult<Boolean> deleteArticle(@RequestParam("id") Long id) {
        articleService.deleteArticle(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章管理")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_article:query')")
    public CommonResult<SalesArticleRespVO> getArticle(@RequestParam("id") Long id) {
        SalesArticleDO article = articleService.getArticle(id);
        return success(SalesArticleConvert.INSTANCE.convert(article));
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章管理分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_article:query')")
    public CommonResult<PageResult<SalesArticleRespVO>> getArticlePage(@Valid SalesArticlePageReqVO pageVO) {
        PageResult<SalesArticleDO> pageResult = articleService.getArticlePage(pageVO);
        return success(SalesArticleConvert.INSTANCE.convertPage(pageResult));
    }

}
