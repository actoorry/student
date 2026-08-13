package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesPointActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesPointActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesPointActivityRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointProductDO;
import vip.appap.suxin.module.sales.service.SalesPointActivityService;
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
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "用户 App - 积分商城活动")
@RestController
@RequestMapping("/sales/promotion/point-activity")
@Validated
public class AppSalesPointActivityController {

    @Resource
    private SalesPointActivityService pointActivityService;

    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/page")
    @Operation(summary = "获得积分商城活动分页")
    @PermitAll
    public CommonResult<PageResult<AppSalesPointActivityRespVO>> getPointActivityPage(AppSalesPointActivityPageReqVO pageReqVO) {
        // 1. 查询满足当前阶段的活动
        PageResult<SalesPointActivityDO> pageResult = pointActivityService.getPointActivityPage(
                BeanUtils.toBean(pageReqVO, SalesPointActivityPageReqVO.class));
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 2. 拼接数据
        List<AppSalesPointActivityRespVO> resultList = buildAppPointActivityRespVOList(pageResult.getList());
        return success(new PageResult<>(resultList, pageResult.getTotal()));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得积分商城活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppSalesPointActivityDetailRespVO> getPointActivity(@RequestParam("id") Long id) {
        // 1. 获取活动
        SalesPointActivityDO activity = pointActivityService.getPointActivity(id);
        if (activity == null
                || ObjUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 2. 拼接数据
        List<SalesPointProductDO> products = pointActivityService.getPointProductListByActivityIds(Collections.singletonList(id));
        SalesPointProductDO minProduct = getMinObject(products, SalesPointProductDO::getPoint);
        assert minProduct != null;
        AppSalesPointActivityDetailRespVO respVO = BeanUtils.toBean(activity, AppSalesPointActivityDetailRespVO.class)
                .setProducts(BeanUtils.toBean(products, AppSalesPointActivityDetailRespVO.Product.class))
                .setPoint(minProduct.getPoint()).setPrice(minProduct.getPrice());
        return success(respVO);
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得积分商城活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    @PermitAll
    public CommonResult<List<AppSalesPointActivityRespVO>> getCombinationActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<SalesPointActivityDO> activityList = pointActivityService.getPointActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<AppSalesPointActivityRespVO> result = buildAppPointActivityRespVOList(activityList);
        return success(result);
    }

    private List<AppSalesPointActivityRespVO> buildAppPointActivityRespVOList(List<SalesPointActivityDO> activityList) {
        List<SalesPointProductDO> products = pointActivityService.getPointProductListByActivityIds(
                convertSet(activityList, SalesPointActivityDO::getId));
        Map<Long, List<SalesPointProductDO>> productsMap = convertMultiMap(products, SalesPointProductDO::getActivityId);
        Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpuMap(
                convertSet(activityList, SalesPointActivityDO::getSpuId));
        List<AppSalesPointActivityRespVO> result = BeanUtils.toBean(activityList, AppSalesPointActivityRespVO.class);
        result.forEach(activity -> {
            // 设置 product 信息
            SalesPointProductDO minProduct = getMinObject(productsMap.get(activity.getId()), SalesPointProductDO::getPoint);
            assert minProduct != null;
            activity.setPoint(minProduct.getPoint()).setPrice(minProduct.getPrice());
            findAndThen(spuMap, activity.getSpuId(),
                    spu -> activity.setSpuName(spu.getName()).setPicUrl(spu.getPicUrl()).setMarketPrice(spu.getMarketPrice()));
        });
        return result;
    }

}
