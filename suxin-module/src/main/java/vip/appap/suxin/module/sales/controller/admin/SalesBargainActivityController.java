package vip.appap.suxin.module.sales.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.*;
import vip.appap.suxin.module.sales.convert.SalesBargainActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.enums.SalesBargainRecordStatusEnum;
import vip.appap.suxin.module.sales.service.SalesBargainActivityService;
import vip.appap.suxin.module.sales.service.SalesBargainHelpService;
import vip.appap.suxin.module.sales.service.SalesBargainRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - 砍价活动")
@RestController
@RequestMapping("/sales/promotion/bargain-activity")
@Validated
public class SalesBargainActivityController {

    @Resource
    private SalesBargainActivityService bargainActivityService;
    @Resource
    private SalesBargainRecordService bargainRecordService;
    @Resource
    private SalesBargainHelpService bargainHelpService;

    @Resource
    private ProductSpuApi spuApi;

    @PostMapping("/create")
    @Operation(summary = "创建砍价活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_activity:create')")
    public CommonResult<Long> createBargainActivity(@Valid @RequestBody SalesBargainActivityCreateReqVO createReqVO) {
        return success(bargainActivityService.createBargainActivity(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新砍价活动")
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_activity:update')")
    public CommonResult<Boolean> updateBargainActivity(@Valid @RequestBody SalesBargainActivityUpdateReqVO updateReqVO) {
        bargainActivityService.updateBargainActivity(updateReqVO);
        return success(true);
    }

    @PutMapping("/close")
    @Operation(summary = "关闭砍价活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_activity:close')")
    public CommonResult<Boolean> closeSeckillActivity(@RequestParam("id") Long id) {
        bargainActivityService.closeBargainActivityById(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除砍价活动")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_activity:delete')")
    public CommonResult<Boolean> deleteBargainActivity(@RequestParam("id") Long id) {
        bargainActivityService.deleteBargainActivity(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得砍价活动")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_activity:query')")
    public CommonResult<SalesBargainActivityRespVO> getBargainActivity(@RequestParam("id") Long id) {
        return success(SalesBargainActivityConvert.INSTANCE.convert(bargainActivityService.getBargainActivity(id)));
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动分页")
    @PreAuthorize("@ss.hasPermission('sales:sales_bargain_activity:query')")
    public CommonResult<PageResult<SalesBargainActivityPageItemRespVO>> getBargainActivityPage(
            @Valid SalesBargainActivityPageReqVO pageVO) {
        // 查询砍价活动
        PageResult<SalesBargainActivityDO> pageResult = bargainActivityService.getBargainActivityPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), SalesBargainActivityDO::getSpuId));
        // 统计数据
        Collection<Long> activityIds = convertList(pageResult.getList(), SalesBargainActivityDO::getId);
        Map<Long, Integer> recordUserCountMap = bargainRecordService.getBargainRecordUserCountMap(activityIds, null);
        Map<Long, Integer> recordSuccessUserCountMap = bargainRecordService.getBargainRecordUserCountMap(activityIds,
                SalesBargainRecordStatusEnum.SUCCESS.getStatus());
        Map<Long, Integer> helpUserCountMap = bargainHelpService.getBargainHelpUserCountMapByActivity(activityIds);
        return success(SalesBargainActivityConvert.INSTANCE.convertPage(pageResult, spuList,
                recordUserCountMap, recordSuccessUserCountMap, helpUserCountMap));
    }

}
