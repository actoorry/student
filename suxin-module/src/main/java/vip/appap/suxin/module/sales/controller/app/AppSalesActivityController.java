package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesActivityRespVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesBargainActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCombinationActivityDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesSeckillActivityDO;
import vip.appap.suxin.module.sales.enums.SalesTypeEnum;
import vip.appap.suxin.module.sales.service.SalesBargainActivityService;
import vip.appap.suxin.module.sales.service.SalesCombinationActivityService;
import vip.appap.suxin.module.sales.service.SalesSeckillActivityService;
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

import java.util.ArrayList;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 营销活动") // 用于提供跨多个活动的 HTTP 接口
@RestController
@RequestMapping("/sales/promotion/activity")
@Validated
public class AppSalesActivityController {

    @Resource
    private SalesCombinationActivityService combinationActivityService;
    @Resource
    private SalesSeckillActivityService seckillActivityService;
    @Resource
    private SalesBargainActivityService bargainActivityService;

    @GetMapping("/list-by-spu-id")
    @Operation(summary = "获得单个商品，进行中的拼团、秒杀、砍价活动信息", description = "每种活动，只返回一个")
    @Parameter(name = "spuId", description = "商品编号", required = true)
    @PermitAll
    public CommonResult<List<AppSalesActivityRespVO>> getActivityListBySpuId(@RequestParam("spuId") Long spuId) {
        List<AppSalesActivityRespVO> activityVOList = new ArrayList<>();
        // 1. 拼团活动
        SalesCombinationActivityDO combinationActivity = combinationActivityService.getMatchCombinationActivityBySpuId(spuId);
        if (combinationActivity != null) {
            activityVOList.add(new AppSalesActivityRespVO(combinationActivity.getId(), SalesTypeEnum.COMBINATION_ACTIVITY.getType(),
                    combinationActivity.getName(), combinationActivity.getSpuId(), combinationActivity.getStartTime(), combinationActivity.getEndTime()));
        }
        // 2. 秒杀活动
        SalesSeckillActivityDO seckillActivity = seckillActivityService.getMatchSeckillActivityBySpuId(spuId);
        if (seckillActivity != null) {
            activityVOList.add(new AppSalesActivityRespVO(seckillActivity.getId(), SalesTypeEnum.SECKILL_ACTIVITY.getType(),
                    seckillActivity.getName(), seckillActivity.getSpuId(), seckillActivity.getStartTime(), seckillActivity.getEndTime()));
        }
        // 3. 砍价活动
        SalesBargainActivityDO bargainActivity = bargainActivityService.getMatchBargainActivityBySpuId(spuId);
        if (bargainActivity != null) {
            activityVOList.add(new AppSalesActivityRespVO(bargainActivity.getId(), SalesTypeEnum.BARGAIN_ACTIVITY.getType(),
                    bargainActivity.getName(), bargainActivity.getSpuId(), bargainActivity.getStartTime(), bargainActivity.getEndTime()));
        }
        return success(activityVOList);
    }

}
