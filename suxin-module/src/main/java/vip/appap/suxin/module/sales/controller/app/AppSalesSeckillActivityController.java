package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils;
import vip.appap.suxin.module.product.api.ProductSpuApi;
import vip.appap.suxin.module.product.api.dto.ProductSpuRespDTO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityDetailRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityNowRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillActivityRespVO;
import vip.appap.suxin.module.sales.convert.SalesSeckillActivityConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillConfigDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillProductDO;
import vip.appap.suxin.module.sales.service.SalesSeckillActivityService;
import vip.appap.suxin.module.sales.service.SalesSeckillConfigService;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.cache.CacheUtils.buildAsyncReloadingCache;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertList;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.findFirst;
import static vip.appap.suxin.framework.common.util.date.LocalDateTimeUtils.isBetween;

@Tag(name = "用户 App - 秒杀活动")
@RestController
@RequestMapping("/sales/promotion/seckill-activity")
@Validated
public class AppSalesSeckillActivityController {

    /**
     * {@link AppSalesSeckillActivityNowRespVO} 缓存，通过它异步刷新 {@link #getNowSeckillActivity()} 所要的首页数据
     */
    private final LoadingCache<String, AppSalesSeckillActivityNowRespVO> nowSeckillActivityCache = buildAsyncReloadingCache(Duration.ofSeconds(10L),
            new CacheLoader<String, AppSalesSeckillActivityNowRespVO>() {

                @Override
                public AppSalesSeckillActivityNowRespVO load(String key) {
                     return getNowSeckillActivity0();
                }

            });

    @Resource
    private SalesSeckillActivityService activityService;
    @Resource
    @Lazy
    private SalesSeckillConfigService configService;

    @Resource
    private ProductSpuApi spuApi;

    @GetMapping("/get-now")
    @Operation(summary = "获得当前秒杀活动", description = "获取当前正在进行的活动，提供给首页使用")
    @PermitAll
    public CommonResult<AppSalesSeckillActivityNowRespVO> getNowSeckillActivity() {
        return success(nowSeckillActivityCache.getUnchecked("")); // 缓存
    }

    private AppSalesSeckillActivityNowRespVO getNowSeckillActivity0() {
        // 1. 获取当前时间处在哪个秒杀阶段
        SalesSeckillConfigDO config = configService.getCurrentSeckillConfig();
        if (config == null) { // 时段不存在直接返回 null
            return new AppSalesSeckillActivityNowRespVO();
        }

        // 2.1 查询满足当前阶段的活动
        List<SalesSeckillActivityDO> activityList = activityService.getSeckillActivityListByConfigIdAndStatus(config.getId(), CommonStatusEnum.ENABLE.getStatus());
        List<SalesSeckillProductDO> productList = activityService.getSeckillProductListByActivityIds(
                convertList(activityList, SalesSeckillActivityDO::getId));
        // 2.2 获取 spu 信息
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(activityList, SalesSeckillActivityDO::getSpuId));
        return SalesSeckillActivityConvert.INSTANCE.convert(config, activityList, productList, spuList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得秒杀活动分页")
    @PermitAll
    public CommonResult<PageResult<AppSalesSeckillActivityRespVO>> getSeckillActivityPage(AppSalesSeckillActivityPageReqVO pageReqVO) {
        // 1. 查询满足当前阶段的活动
        PageResult<SalesSeckillActivityDO> pageResult = activityService.getSeckillActivityAppPageByConfigId(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        List<SalesSeckillProductDO> productList = activityService.getSeckillProductListByActivityIds(
                convertList(pageResult.getList(), SalesSeckillActivityDO::getId));

        // 2. 拼接数据
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(pageResult.getList(), SalesSeckillActivityDO::getSpuId));
        return success(SalesSeckillActivityConvert.INSTANCE.convertPage02(pageResult, productList, spuList));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得秒杀活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    @PermitAll
    public CommonResult<AppSalesSeckillActivityDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        // 1. 获取活动
        SalesSeckillActivityDO activity = activityService.getSeckillActivity(id);
        if (activity == null
                || ObjectUtil.equal(activity.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            return success(null);
        }

        // 2. 获取时间段
        List<SalesSeckillConfigDO> configs = configService.getSeckillConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        configs.removeIf(config -> !CollUtil.contains(activity.getConfigIds(), config.getId()));
        // 2.1 优先使用当前时间段
        SalesSeckillConfigDO config = findFirst(configs, config0 -> isBetween(config0.getStartTime(), config0.getEndTime()));
        // 2.2 如果没有，则获取最后一个，因为倾向优先展示"未开始" > "已结束"
        if (config == null) {
            config = CollUtil.getLast(configs);
        }
        if (config == null) {
            return null;
        }
        // 3. 计算开始时间、结束时间
        LocalDate nowDate;
        // 3.1 如果在活动日期范围内，则以今天为 nowDate
        if (LocalDateTimeUtils.isBetween(activity.getStartTime(), activity.getEndTime())) {
            nowDate = LocalDate.now();
        } else {
            // 3.2 如果不在活动时间范围内，则直接以活动的 endTime 作为 nowDate，因为还是倾向优先展示"未开始" > "已结束"
            nowDate = activity.getEndTime().toLocalDate();
        }
        LocalDateTime startTime = LocalDateTime.of(nowDate, LocalTime.parse(config.getStartTime()));
        LocalDateTime endTime = LocalDateTime.of(nowDate, LocalTime.parse(config.getEndTime()));

        // 4. 拼接数据
        List<SalesSeckillProductDO> productList = activityService.getSeckillProductListByActivityId(activity.getId());
        return success(SalesSeckillActivityConvert.INSTANCE.convert3(activity, productList, startTime, endTime));
    }

    @GetMapping("/list-by-ids")
    @Operation(summary = "获得秒杀活动列表，基于活动编号数组")
    @Parameter(name = "ids", description = "活动编号数组", required = true, example = "[1024, 1025]")
    @PermitAll
    public CommonResult<List<AppSalesSeckillActivityRespVO>> getCombinationActivityListByIds(@RequestParam("ids") List<Long> ids) {
        // 1. 获得开启的活动列表
        List<SalesSeckillActivityDO> activityList = activityService.getSeckillActivityListByIds(ids);
        activityList.removeIf(activity -> CommonStatusEnum.isDisable(activity.getStatus()));
        if (CollUtil.isEmpty(activityList)) {
            return success(Collections.emptyList());
        }
        // 2. 拼接返回
        List<SalesSeckillProductDO> productList = activityService.getSeckillProductListByActivityIds(
                convertList(activityList, SalesSeckillActivityDO::getId));
        List<ProductSpuRespDTO> spuList = spuApi.getSpuList(convertList(activityList, SalesSeckillActivityDO::getSpuId));
        return success(SalesSeckillActivityConvert.INSTANCE.convertAppList(activityList, productList, spuList));
    }

}
