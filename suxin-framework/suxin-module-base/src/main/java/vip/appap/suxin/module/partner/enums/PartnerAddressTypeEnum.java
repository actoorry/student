package vip.appap.suxin.module.partner.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 会员地址类型枚举
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum PartnerAddressTypeEnum {

    MEMBER(0, "会员收件地址"),
    SENDER(1, "商户寄件地址");

    private final Integer type;
    private final String name;

    public static boolean isSender(Integer type) {
        return SENDER.getType().equals(type);
    }

    public static boolean isMember(Integer type) {
        return MEMBER.getType().equals(type);
    }

    public static PartnerAddressTypeEnum valueOfType(Integer type) {
        return Arrays.stream(values()).filter(item -> item.getType().equals(type)).findFirst().orElse(null);
    }

}
