package vip.appap.suxin.module.sales.job.promotion.coupon;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.quartz.core.handler.JobHandler;
import vip.appap.suxin.framework.tenant.core.job.TenantJob;
import vip.appap.suxin.module.sales.service.SalesCouponService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 优惠券过期 Job
 *
 * @author owen
 */
@Component
public class SalesCouponExpireJob implements JobHandler {

    @Resource
    private SalesCouponService couponService;

    @Override
    @TenantJob
    public String execute(String param) {
        int count = couponService.expireCoupon();
        return StrUtil.format("过期优惠券 {} 个", count);
    }

}
