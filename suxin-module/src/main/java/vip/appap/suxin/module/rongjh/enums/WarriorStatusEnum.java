package vip.appap.suxin.module.rongjh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 战友会/三属 身份状态
 */
@Getter
@AllArgsConstructor
public enum WarriorStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");

    private final Integer status;
    private final String name;

    public static String getName(Integer status) {
        return Arrays.stream(values())
                .filter(item -> item.status.equals(status))
                .map(WarriorStatusEnum::getName)
                .findFirst()
                .orElse("未知");
    }

}
