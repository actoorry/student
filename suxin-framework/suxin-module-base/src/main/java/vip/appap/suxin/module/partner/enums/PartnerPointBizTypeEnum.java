package vip.appap.suxin.module.partner.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户积分业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum PartnerPointBizTypeEnum {

    ORDER_USE(1, "订单抵扣"),
    ORDER_GIVE(2, "订单赠送"),
    ORDER_USE_CANCEL(3, "订单取消回滚抵扣"),
    ORDER_GIVE_CANCEL(4, "订单取消回滚赠送"),
    ORDER_USE_CANCEL_ITEM(5, "订单项取消回滚抵扣"),
    ORDER_GIVE_CANCEL_ITEM(6, "订单项取消回滚赠送");

    private final Integer type;
    private final String name;
}
