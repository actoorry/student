package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCombinationActivityRespVO;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import vip.appap.suxin.module.sales.service.SalesCombinationActivityService;
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
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/sales/promotion/combination-activity")
@Validated
public class AppSalesCombinationActivityController {

    @Resource
    private SalesCombinationActivityService activityService;

    @Resource
    private ProductSpuApi spuApi;

    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    @PermitAll
    public CommonResult<PageResult<AppSalesCombinationActivityRespVO>> getCombinationActivityPage(PageParam pageParam) {
        PageResult<SalesCombinationActivityDO> pageResult = activityService.getCombinationActivityPage(pageParam);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 拼接返回
        List<SalesCombinationProductDO> productList = activityService.getCombinationProductListByActivityIds(
                convertList(pageResult.getList(), SalesCombinationActivityDO::getId));
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), SalesCombinationActivityDO::getSpuId));
        return success(SalesCombinationActivityConvert.INSTANCE.convertAppPage(pageResult, productList, spuList));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得拼团活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    @PermitAll
    public CommonResult<List<AppSalesCombinationActivityRespVO>> getCombinationActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<SalesCombinationActivityDO> activityList = activityService.getCombinationActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<SalesCombinationProductDO> productList = activityService.getCombinationProductListByActivityIds(
                convertList(activityList, SalesCombinationActivityDO::getId));
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(activityList, SalesCombinationActivityDO::getSpuId));
        return success(SalesCombinationActivityConvert.INSTANCE.convertAppList(activityList, productList, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppSalesCombinationActivityDetailRespVO> getCombinationActivityDetail(@RequestParam("id") Long id) {
        // 1. 获取活动
        SalesCombinationActivityDO activity = activityService.getCombinationActivity(id);
        if (activity == null
                || ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 2. 获取活动商品
        List<SalesCombinationProductDO> products = activityService.getCombinationProductsByActivityId(activity.getId());
        return success(SalesCombinationActivityConvert.INSTANCE.convert3(activity, products));
    }

}
