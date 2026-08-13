package vip.appap.suxin.module.accountant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 微信虚拟支付发货状态。
 */
@Getter
@AllArgsConstructor
public enum PayOrderWechatVirtualDeliverStatusEnum {

    WAITING(0, "待发货确认"),
    DELIVERED(1, "已确认发货"),
    FAILED(2, "发货确认失败");

    private final Integer status;
    private final String name;

}
