package vip.appap.suxin.module.sales.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易售后 - 方式
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum SalesAfterSaleWayEnum implements ArrayValuable<Integer> {

    REFUND(10, "仅退款"),
    RETURN_AND_REFUND(20, "退货退款");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesAfterSaleWayEnum::getWay).toArray(Integer[]::new);

    /**
     * 方式
     */
    private final Integer way;
    /**
     * 方式名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
