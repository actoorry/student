package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticlePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticleRespVO;
import vip.appap.suxin.module.sales.convert.SalesArticleConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleDO;
import vip.appap.suxin.module.sales.service.SalesArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 文章")
@RestController
@RequestMapping("/sales/promotion/article")
@Validated
public class AppSalesArticleController {

    @Resource
    private SalesArticleService articleService;

    @RequestMapping("/list")
    @Operation(summary = "获得文章详情列表")
    @Parameters({
            @Parameter(name = "recommendHot", description = "是否热门", example = "false"), // 场景一：查看指定的文章
            @Parameter(name = "recommendBanner", description = "是否轮播图", example = "false") // 场景二：查看指定的文章
    })
    @PermitAll
    public CommonResult<List<AppSalesArticleRespVO>> getArticleList(
            @RequestParam(value = "recommendHot", required = false) Boolean recommendHot,
            @RequestParam(value = "recommendBanner", required = false) Boolean recommendBanner) {
        return success(SalesArticleConvert.INSTANCE.convertList03(
                articleService.getArticleCategoryListByRecommend(recommendHot, recommendBanner)));
    }

    @RequestMapping("/page")
    @Operation(summary = "获得文章详情分页")
    @PermitAll
    public CommonResult<PageResult<AppSalesArticleRespVO>> getArticlePage(AppSalesArticlePageReqVO pageReqVO) {
        return success(SalesArticleConvert.INSTANCE.convertPage02(articleService.getArticlePage(pageReqVO)));
    }

    @RequestMapping("/get")
    @Operation(summary = "获得文章详情")
    @Parameters({
            @Parameter(name = "id", description = "文章编号", example = "1024"),
            @Parameter(name = "title", description = "文章标题", example = "1024"),
    })
    @PermitAll
    public CommonResult<AppSalesArticleRespVO> getArticle(@RequestParam(value = "id", required = false) Long id,
                                                     @RequestParam(value = "title", required = false) String title) {
        SalesArticleDO article = id != null ? articleService.getArticle(id)
                : articleService.getLastArticleByTitle(title);
        return success(BeanUtils.toBean(article, AppSalesArticleRespVO.class));
    }

    @PutMapping("/add-browse-count")
    @Operation(summary = "增加文章浏览量")
    @Parameter(name = "id", description = "文章编号", example = "1024")
    @PermitAll
    public CommonResult<Boolean> addBrowseCount(@RequestParam("id") Long id) {
        articleService.addArticleBrowseCount(id);
        return success(true);
    }

}