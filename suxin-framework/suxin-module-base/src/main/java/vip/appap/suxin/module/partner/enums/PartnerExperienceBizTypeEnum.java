package vip.appap.suxin.module.partner.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户经验业务类型枚举
 */
@Getter
@AllArgsConstructor
public enum PartnerExperienceBizTypeEnum {

    ORDER_GIVE(1, "订单赠送"),
    ORDER_GIVE_CANCEL(2, "订单取消回滚赠送"),
    ORDER_GIVE_CANCEL_ITEM(3, "订单项取消回滚赠送");

    private final Integer type;
    private final String name;
}
