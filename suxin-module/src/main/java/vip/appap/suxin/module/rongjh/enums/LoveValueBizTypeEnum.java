package vip.appap.suxin.module.rongjh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoveValueBizTypeEnum {

    ORDER_RECEIVE("ORDER_RECEIVE"),
    ORDER_PAY_VIRTUAL("ORDER_PAY_VIRTUAL"),
    ORDER_CANCEL("ORDER_CANCEL"),
    ORDER_ITEM_REFUND("ORDER_ITEM_REFUND"),
    BACKFILL("BACKFILL");

    private final String code;
}
