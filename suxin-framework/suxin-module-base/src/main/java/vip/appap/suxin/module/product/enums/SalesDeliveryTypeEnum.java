package vip.appap.suxin.module.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vip.appap.suxin.framework.common.core.ArrayValuable;

import java.util.Arrays;

/**
 * 配送方式枚举
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum SalesDeliveryTypeEnum implements ArrayValuable<Integer> {

    ONLINE(0, "线上发货"),
    EXPRESS(1, "快递发货"),
    PICK_UP(2, "用户自提"),
    AUTO(3, "自动发货");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesDeliveryTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 配送方式
     */
    private final Integer type;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
