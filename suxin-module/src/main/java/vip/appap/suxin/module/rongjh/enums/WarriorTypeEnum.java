package vip.appap.suxin.module.rongjh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 戎集汇身份类型
 */
@Getter
@AllArgsConstructor
public enum WarriorTypeEnum {

    SELF("SELF", "战友"),
    MARTYR("MARTYR", "烈士遗属"),
    SACRIFICE("SACRIFICE", "因公牺牲遗属"),
    ILLNESS("ILLNESS", "病故遗属"),
    FAMILY("FAMILY", "军人家属");

    private final String type;
    private final String name;

    public static String getName(String type) {
        return Arrays.stream(values())
                .filter(item -> item.type.equals(type))
                .map(WarriorTypeEnum::getName)
                .findFirst()
                .orElse("战友");
    }

}
