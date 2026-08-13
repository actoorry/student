package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesSeckillActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillProductDO;
import vip.appap.suxin.module.sales.service.SalesSeckillActivityService;
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

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 秒杀活动")
@RestController
@RequestMapping("/sales/promotion/seckill-activity")
@Validated
public class SalesSeckillActivityController {

    @Resource
    private SalesSeckillActivityService seckillActivityService;
    @Resource
    private ProductSpuApi productSpuApi;

    @PostMapping("/create")
    @Operation(summary = "创建秒杀活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_seckill_activity:create')")
    public CommonResult<Long> createSeckillActivity(@Valid @RequestBody SalesSeckillActivityCreateReqVO createReqVO) {
        return success(seckillActivityService.createSeckillActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新秒杀活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_seckill_activity:update')")
    public CommonResult<Boolean> updateSeckillActivity(@Valid @RequestBody SalesSeckillActivityUpdateReqVO updateReqVO) {
        seckillActivityService.updateSeckillActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭秒杀活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_seckill_activity:close')")
    public CommonResult<Boolean> closeSeckillActivity(@RequestParam("id") Long id) {
        seckillActivityService.closeSeckillActivity(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除秒杀活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_seckill_activity:delete')")
    public CommonResult<Boolean> deleteSeckillActivity(@RequestParam("id") Long id) {
        seckillActivityService.deleteSeckillActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得秒杀活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_seckill_activity:query')")
    public CommonResult<SalesSeckillActivityDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        SalesSeckillActivityDO activity = seckillActivityService.getSeckillActivity(id);
        List<SalesSeckillProductDO> products = seckillActivityService.getSeckillProductListByActivityId(id);
        return success(SalesSeckillActivityConvert.INSTANCE.convert(activity, products));
    }

    @GetMapping("/page")
    @Operation(summary = "获得秒杀活动分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_seckill_activity:query')")
    public CommonResult<PageResult<SalesSeckillActivityRespVO>> getSeckillActivityPage(@Valid SalesSeckillActivityPageReqVO pageVO) {
        // 查询活动列表
        PageResult<SalesSeckillActivityDO> pageResult = seckillActivityService.getSeckillActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<SalesSeckillProductDO> products = seckillActivityService.getSeckillProductListByActivityIds(
                convertSet(pageResult.getList(), SalesSeckillActivityDO::getId));
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(
                convertSet(pageResult.getList(), SalesSeckillActivityDO::getSpuId));
        return success(SalesSeckillActivityConvert.INSTANCE.convertPage(pageResult, products, spuList));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得秒杀活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    public CommonResult<List<SalesSeckillActivityRespVO>> getSeckillActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<SalesSeckillActivityDO> activityList = seckillActivityService.getSeckillActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<SalesSeckillProductDO> productList = seckillActivityService.getSeckillProductListByActivityIds(
                convertList(activityList, SalesSeckillActivityDO::getId));
        List<ProductSpuRespDTO> spuList = productSpuApi.getSpuList(convertList(activityList, SalesSeckillActivityDO::getSpuId));
        return success(SalesSeckillActivityConvert.INSTANCE.convertList(activityList, productList, spuList));
    }

}
