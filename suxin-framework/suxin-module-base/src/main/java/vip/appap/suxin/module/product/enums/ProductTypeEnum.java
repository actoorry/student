package vip.appap.suxin.module.product.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTypeEnum {
    ENTITY(1, "实体产品"),
    SERVICE(2, "服务产品"),
    COMBINATION(3, "组合产品"),
    MEMBER(4, "会员产品");

    private final Integer value;
    private final String name;

    public static ProductTypeEnum valueOf(Integer value) {
        for (ProductTypeEnum typeEnum : values()) {
            if (ObjectUtil.equal(typeEnum.value, value)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static boolean isVirtualLike(Integer type) {
        return ObjectUtil.equal(type, SERVICE.value) || ObjectUtil.equal(type, MEMBER.value);
    }

    public static boolean requiresFulfillmentUnit(Integer type) {
        return isVirtualLike(type);
    }

    public static boolean isMember(Integer type) {
        return ObjectUtil.equal(type, MEMBER.value);
    }

}
