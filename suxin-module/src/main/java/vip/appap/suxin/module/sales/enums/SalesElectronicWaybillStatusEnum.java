package vip.appap.suxin.module.sales.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 电子面单订单记录状态枚举
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum SalesElectronicWaybillStatusEnum {

    FAILED(0, "下单失败"),
    VALID(1, "有效"),
    CANCELED(2, "已作废"),
    UNKNOWN(3, "结果待确认");

    private final Integer status;
    private final String name;

    public static boolean isValid(Integer status) {
        return VALID.getStatus().equals(status);
    }

    public static boolean isCanceled(Integer status) {
        return CANCELED.getStatus().equals(status);
    }

    public static SalesElectronicWaybillStatusEnum getByStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (SalesElectronicWaybillStatusEnum item : values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

}
