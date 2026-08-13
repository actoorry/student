package vip.appap.suxin.module.product.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.controller.admin.vo.ProductCategoryListReqVO;
import vip.appap.suxin.module.product.controller.app.vo.AppCategoryRespVO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.module.product.enums.ProductCategoryDimensionConstants.SALES_ROOT_ID;

@Tag(name = "用户 APP - 商品分类")
@RestController
@RequestMapping("/product/category")
@Validated
public class AppCategoryController {

    @Resource
    private ProductCategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获得商品分类列表")
    @PermitAll
    public CommonResult<List<AppCategoryRespVO>> getProductCategoryList() {
        ProductCategoryListReqVO reqVO = new ProductCategoryListReqVO();
        reqVO.setParentId(SALES_ROOT_ID); // 手机端始终获取虚拟销售维度
        reqVO.setStatus(0); // 启用
        List<ProductCategoryDO> list = categoryService.getCategoryList(reqVO);
        list.sort(Comparator.comparing(ProductCategoryDO::getSort));
        return success(BeanUtils.toBean(list, AppCategoryRespVO.class));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得商品分类列表，指定编号")
    @Parameter(name = "ids", description = "商品分类编号数组", required = true)
    @PermitAll
    public CommonResult<List<AppCategoryRespVO>> getProductCategoryList(@RequestParam("ids") List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return success(Collections.emptyList());
        }
        List<ProductCategoryDO> list = categoryService.getEnableCategoryList(ids);
        list.sort(Comparator.comparing(ProductCategoryDO::getSort));
        return success(BeanUtils.toBean(list, AppCategoryRespVO.class));
    }

}
