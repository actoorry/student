package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesArticleCategoryRespVO;
import vip.appap.suxin.module.sales.convert.SalesArticleCategoryConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesArticleCategoryDO;
import vip.appap.suxin.module.sales.service.SalesArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Comparator;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 文章分类")
@RestController
@RequestMapping("/sales/promotion/article-category")
@Validated
public class AppSalesArticleCategoryController {

    @Resource
    private SalesArticleCategoryService articleCategoryService;

    @RequestMapping("/list")
    @Operation(summary = "获得文章分类列表")
    public CommonResult<List<AppSalesArticleCategoryRespVO>> getArticleCategoryList() {
        List<SalesArticleCategoryDO> categoryList = articleCategoryService.getArticleCategoryListByStatus(
                CommonStatusEnum.ENABLE.getStatus());
        categoryList.sort(Comparator.comparing(SalesArticleCategoryDO::getSort)); // 按 sort 降序排列
        return success(SalesArticleCategoryConvert.INSTANCE.convertList04(categoryList));
    }

}
