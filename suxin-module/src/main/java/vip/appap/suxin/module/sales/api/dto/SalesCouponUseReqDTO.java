package vip.appap.suxin.module.sales.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 优惠劵使用 Request DTO
 *
 * @author 书心软件
 */
@Data
public class SalesCouponUseReqDTO {

    /**
     * 优惠劵编号
     */
    @NotNull(message = "优惠劵编号不能为空")
    private Long id;

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

}
