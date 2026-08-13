package vip.appap.suxin.module.sales.controller.app;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCouponPageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCouponRespVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesCouponTakeReqVO;
import vip.appap.suxin.module.sales.convert.SalesCouponConvert;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesCouponTemplateDO;
import vip.appap.suxin.module.sales.enums.SalesCouponTakeTypeEnum;
import vip.appap.suxin.module.sales.service.SalesCouponService;
import vip.appap.suxin.module.sales.service.SalesCouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 优惠劵")
@RestController
@RequestMapping("/sales/promotion/coupon")
@Validated
public class AppSalesCouponController {

    @Resource
    private SalesCouponService couponService;
    @Resource
    private SalesCouponTemplateService couponTemplateService;

    @PostMapping("/take")
    @Operation(summary = "领取优惠劵")
    @Parameter(name = "templateId", description = "优惠券模板编号", required = true, example = "1024")
    public CommonResult<Boolean> takeCoupon(@Valid @RequestBody AppSalesCouponTakeReqVO reqVO) {
        // 1. 领取优惠劵
        Long userId = getLoginUserId();
        couponService.takeCoupon(reqVO.getTemplateId(), CollUtil.newHashSet(userId), SalesCouponTakeTypeEnum.USER);

        // 2. 检查是否可以继续领取
        SalesCouponTemplateDO couponTemplate = couponTemplateService.getCouponTemplate(reqVO.getTemplateId());
        boolean canTakeAgain = true;
        if (couponTemplate.getTakeLimitCount() != null && couponTemplate.getTakeLimitCount() > 0) {
            Integer takeCount = couponService.getTakeCount(reqVO.getTemplateId(), userId);
            canTakeAgain = takeCount < couponTemplate.getTakeLimitCount();
        }
        return success(canTakeAgain);
    }

    @GetMapping("/page")
    @Operation(summary = "我的优惠劵列表")
    public CommonResult<PageResult<AppSalesCouponRespVO>> getCouponPage(AppSalesCouponPageReqVO pageReqVO) {
        PageResult<SalesCouponDO> pageResult = couponService.getCouponPage(
                SalesCouponConvert.INSTANCE.convert(pageReqVO, Collections.singleton(getLoginUserId())));
        return success(BeanUtils.toBean(pageResult, AppSalesCouponRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得优惠劵")
    @Parameter(name = "id", description = "优惠劵编号", required = true, example = "1024")
    public CommonResult<AppSalesCouponRespVO> getCoupon(@RequestParam("id") Long id) {
        SalesCouponDO coupon = couponService.getCoupon(getLoginUserId(), id);
        return success(BeanUtils.toBean(coupon, AppSalesCouponRespVO.class));
    }

    @GetMapping(value = "/get-unused-count")
    @Operation(summary = "获得未使用的优惠劵数量")
    public CommonResult<Long> getUnusedCouponCount() {
        return success(couponService.getUnusedCouponCount(getLoginUserId()));
    }

}
