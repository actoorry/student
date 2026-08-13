package vip.appap.suxin.module.rongjh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 爱心帮扶申请状态
 */
@Getter
@AllArgsConstructor
public enum HelpStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回"),
    CANCELLED(3, "已撤销");

    private final Integer status;
    private final String name;

    public static String getName(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .map(HelpStatusEnum::getName)
                .findFirst()
                .orElse("未知");
    }

}
