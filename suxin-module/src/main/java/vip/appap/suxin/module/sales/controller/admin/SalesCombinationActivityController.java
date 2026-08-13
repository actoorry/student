package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesCombinationActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationProductDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationRecordDO;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import vip.appap.suxin.module.sales.service.SalesCombinationActivityService;
import vip.appap.suxin.module.sales.service.SalesCombinationRecordService;
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
import java.util.Set;

import static cn.hutool.core.collection.CollectionUtil.newArrayList;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 拼团活动")
@RestController
@RequestMapping("/sales/promotion/combination-activity")
@Validated
public class SalesCombinationActivityController {

    @Resource
    private SalesCombinationActivityService combinationActivityService;
    @Resource
    private SalesCombinationRecordService combinationRecordService;

    @Resource
    private ProductSpuApi productSpuApi;

    @PostMapping("/create")
    @Operation(summary = "创建拼团活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_activity:create')")
    public CommonResult<Long> createCombinationActivity(@Valid @RequestBody SalesCombinationActivityCreateReqVO createReqVO) {
        return success(combinationActivityService.createCombinationActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新拼团活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_activity:update')")
    public CommonResult<Boolean> updateCombinationActivity(@Valid @RequestBody SalesCombinationActivityUpdateReqVO updateReqVO) {
        combinationActivityService.updateCombinationActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭拼团活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_activity:close')")
    public CommonResult<Boolean> closeCombinationActivity(@RequestParam("id") Long id) {
        combinationActivityService.closeCombinationActivityById(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除拼团活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_activity:delete')")
    public CommonResult<Boolean> deleteCombinationActivity(@RequestParam("id") Long id) {
        combinationActivityService.deleteCombinationActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得拼团活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_activity:query')")
    public CommonResult<SalesCombinationActivityRespVO> getCombinationActivity(@RequestParam("id") Long id) {
        SalesCombinationActivityDO activity = combinationActivityService.getCombinationActivity(id);
        List<SalesCombinationProductDO> products = combinationActivityService.getCombinationProductListByActivityIds(newArrayList(id));
        return success(SalesCombinationActivityConvert.INSTANCE.convert(activity, products));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得拼团活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    public CommonResult<List<SalesCombinationActivityRespVO>> getCombinationActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<SalesCombinationActivityDO> activityList = combinationActivityService.getCombinationActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<SalesCombinationProductDO> productList = combinationActivityService.getCombinationProductListByActivityIds(
                convertList(activityList, SalesCombinationActivityDO::getId));
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(convertList(activityList, SalesCombinationActivityDO::getSpuId));
        return success(SalesCombinationActivityConvert.INSTANCE.convertList(activityList, productList, spuList));
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_combination_activity:query')")
    public CommonResult<PageResult<SalesCombinationActivityPageItemRespVO>> getCombinationActivityPage(
            @Valid SalesCombinationActivityPageReqVO pageVO) {
        // 查询拼团活动
        PageResult<SalesCombinationActivityDO> pageResult = combinationActivityService.getCombinationActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 统计数据
        Set<Long> activityIds = convertSet(pageResult.getList(), SalesCombinationActivityDO::getId);
        Map<Long, Integer> groupCountMap = combinationRecordService.getCombinationRecordCountMapByActivity(
                activityIds, null, SalesCombinationRecordDO.HEAD_ID_GROUP);
        Map<Long, Integer> groupSuccessCountMap = combinationRecordService.getCombinationRecordCountMapByActivity(
                activityIds, SalesCombinationRecordStatusEnum.SUCCESS.getStatus(), SalesCombinationRecordDO.HEAD_ID_GROUP);
        Map<Long, Integer> recordCountMap = combinationRecordService.getCombinationRecordCountMapByActivity(
                activityIds, null, null);
        // 拼接数据
        List<SalesCombinationProductDO> products = combinationActivityService.getCombinationProductListByActivityIds(
                convertSet(pageResult.getList(), SalesCombinationActivityDO::getId));
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(
                convertSet(pageResult.getList(), SalesCombinationActivityDO::getSpuId));
        return success(SalesCombinationActivityConvert.INSTANCE.convertPage(pageResult, products,
                groupCountMap, groupSuccessCountMap, recordCountMap, spus));
    }

}
