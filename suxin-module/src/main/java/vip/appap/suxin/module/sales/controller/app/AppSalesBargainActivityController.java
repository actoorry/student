package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesBargainActivityRespVO;
import vip.appap.suxin.module.sales.convert.SalesBargainActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.enums.SalesBargainRecordStatusEnum;
import vip.appap.suxin.module.sales.service.SalesBargainActivityService;
import vip.appap.suxin.module.sales.service.SalesBargainRecordService;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "用户 App - 砍价活动")
@RestController
@RequestMapping("/sales/promotion/bargain-activity")
@Validated
public class AppSalesBargainActivityController {

    /**
     * {@link AppSalesBargainActivityRespVO} 缓存，通过它异步刷新 {@link #getBargainActivityList0(Integer)} 所要的首页数据
     */
    private final LoadingCache<Integer, List<AppSalesBargainActivityRespVO>> bargainActivityListCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<Integer, List<AppSalesBargainActivityRespVO>>() {

                @Override
                public List<AppSalesBargainActivityRespVO> load(Integer count) {
                    return getBargainActivityList0(count);
                }

            });

    @Resource
    private SalesBargainActivityService bargainActivityService;
    @Resource
    private SalesBargainRecordService bargainRecordService;

    @Resource
    private ProductSpuApi spuApi;

    @GetMapping("/list")
    @Operation(summary = "获得砍价活动列表", description = "用于小程序首页")
    @Parameter(name = "count", description = "需要展示的数量", example = "6")
    @PermitAll
    public CommonResult<List<AppSalesBargainActivityRespVO>> getBargainActivityList(
            @RequestParam(name = "count", defaultValue = "6") Integer count) {
        return success(bargainActivityListCache.getUnchecked(count));
    }

    private List<AppSalesBargainActivityRespVO>getBargainActivityList0(Integer count) {
        List<SalesBargainActivityDO> list = bargainActivityService.getBargainActivityListByCount(count);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(list, SalesBargainActivityDO::getSpuId));
        return SalesBargainActivityConvert.INSTANCE.convertAppList(list, spuList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得砍价活动分页")
    @PermitAll
    public CommonResult<PageResult<AppSalesBargainActivityRespVO>> getBargainActivityPage(PageParam pageReqVO) {
        PageResult<SalesBargainActivityDO> result = bargainActivityService.getBargainActivityPage(pageReqVO);
        if (CollUtil.isEmpty(result.getList())) {
            return success(PageResult.empty(result.getTotal()));
        }
        // 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(result.getList(), SalesBargainActivityDO::getSpuId));
        return success(SalesBargainActivityConvert.INSTANCE.convertAppPage(result, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得砍价活动详情")
    @Parameter(name = "id", description = "活动编号", example = "1")
    @PermitAll
    public CommonResult<AppSalesBargainActivityDetailRespVO> getBargainActivityDetail(@RequestParam("id") Long id) {
        SalesBargainActivityDO activity = bargainActivityService.getBargainActivity(id);
        if (activity == null) {
            return success(null);
        }
        // 拼接数据
        Integer successUserCount = bargainRecordService.getBargainRecordUserCount(id, SalesBargainRecordStatusEnum.SUCCESS.getStatus());
        ProductSpuRespDTO spu = spuApi.getSpu(activity.getSpuId());
        return success(SalesBargainActivityConvert.INSTANCE.convert(activity, successUserCount, spu));
    }

}
