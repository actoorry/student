package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivityRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointActivitySaveReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesPointProductRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesPointProductDO;
import vip.appap.suxin.module.sales.service.SalesPointActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - 积分商城活动")
@RestController
@RequestMapping("/sales/promotion/point-activity")
@Validated
public class SalesPointActivityController {

    @Resource
    private SalesPointActivityService pointActivityService;
    @Resource
    private ProductSpuApi productSpuApi;

    @PostMapping("/create")
    @Operation(summary = "创建积分商城活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_point_activity:create')")
    public CommonResult<Long> createPointActivity(@Valid @RequestBody SalesPointActivitySaveReqVO createReqVO) {
        return success(pointActivityService.createPointActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新积分商城活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_point_activity:update')")
    public CommonResult<Boolean> updatePointActivity(@Valid @RequestBody SalesPointActivitySaveReqVO updateReqVO) {
        pointActivityService.updatePointActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭积分商城活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_point_activity:close')")
    public CommonResult<Boolean> closeSeckillActivity(@RequestParam("id") Long id) {
        pointActivityService.closePointActivity(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除积分商城活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_point_activity:delete')")
    public CommonResult<Boolean> deletePointActivity(@RequestParam("id") Long id) {
        pointActivityService.deletePointActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得积分商城活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_point_activity:query')")
    public CommonResult<SalesPointActivityRespVO> getPointActivity(@RequestParam("id") Long id) {
        SalesPointActivityDO pointActivity = pointActivityService.getPointActivity(id);
        if (pointActivity == null) {
            return success(null);
        }

        List<SalesPointProductDO> products = pointActivityService.getPointProductListByActivityIds(Collections.singletonList(id));
        SalesPointActivityRespVO respVO = BeanUtils.toBean(pointActivity, SalesPointActivityRespVO.class);
        respVO.setProducts(BeanUtils.toBean(products, SalesPointProductRespVO.class));
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得积分商城活动分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_point_activity:query')")
    public CommonResult<PageResult<SalesPointActivityRespVO>> getPointActivityPage(@Valid SalesPointActivityPageReqVO pageReqVO) {
        PageResult<SalesPointActivityDO> pageResult = pointActivityService.getPointActivityPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<SalesPointActivityRespVO> resultList = buildPointActivityRespVOList(pageResult.getList());
        return success(new PageResult<>(resultList, pageResult.getTotal()));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得积分商城活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    public CommonResult<List<SalesPointActivityRespVO>> getPointActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<SalesPointActivityDO> activityList = pointActivityService.getPointActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<SalesPointActivityRespVO> result = buildPointActivityRespVOList(activityList);
        return success(result);
    }

    private List<SalesPointActivityRespVO> buildPointActivityRespVOList(List<SalesPointActivityDO> activityList) {
        List<SalesPointProductDO> products = pointActivityService.getPointProductListByActivityIds(
                convertSet(activityList, SalesPointActivityDO::getId));
        Map<Long, List<SalesPointProductDO>> productsMap = convertMultiMap(products, SalesPointProductDO::getActivityId);
        Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpuMap(
                convertSet(activityList, SalesPointActivityDO::getSpuId));
        List<SalesPointActivityRespVO> result = BeanUtils.toBean(activityList, SalesPointActivityRespVO.class);
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