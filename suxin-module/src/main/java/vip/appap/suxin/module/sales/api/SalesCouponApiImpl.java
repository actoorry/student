package vip.appap.suxin.module.sales.api;


import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.sales.api.dto.SalesCouponRespDTO;
import vip.appap.suxin.module.sales.api.dto.SalesCouponUseReqDTO;
import vip.appap.suxin.module.sales.service.SalesCouponService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

/**
 * 优惠劵 API 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class SalesCouponApiImpl implements SalesCouponApi {

    @Resource
    private SalesCouponService couponService;

    @Override
    public List<SalesCouponRespDTO> getCouponListByUserId(Long userId, Integer status) {
        return BeanUtils.toBean(couponService.getCouponList(userId, status), SalesCouponRespDTO.class);
    }

    @Override
    public void useCoupon(SalesCouponUseReqDTO useReqDTO) {
        couponService.useCoupon(useReqDTO.getId(), useReqDTO.getUserId(),
                useReqDTO.getOrderId());
    }

    @Override
    public void returnUsedCoupon(Long id) {
        couponService.returnUsedCoupon(id);
    }

    @Override
    public List<Long> takeCouponsByAdmin(Map<Long, Integer> giveCoupons, Long userId) {
        return couponService.takeCouponsByAdmin(giveCoupons, userId);
    }

    @Override
    public void invalidateCouponsByAdmin(List<Long> giveCouponIds, Long userId) {
        couponService.invalidateCouponsByAdmin(giveCouponIds, userId);
    }

}
