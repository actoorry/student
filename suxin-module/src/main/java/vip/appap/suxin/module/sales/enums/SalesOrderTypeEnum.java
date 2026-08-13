package vip.appap.suxin.module.sales.enums;

import cn.hutool.core.util.ObjectUtil;
import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 交易订单 - 类型
 *
 * @author Sin
 */
@RequiredArgsConstructor
@Getter
public enum SalesOrderTypeEnum implements ArrayValuable<Integer> {

    NORMAL(0, "普通订单"),
    SECKILL(1, "秒杀订单"),
    BARGAIN(2, "砍价订单"),
    COMBINATION(3, "拼团订单"),
    POINT(4, "积分商城"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesOrderTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 类型名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isNormal(Integer type) {
        return ObjectUtil.equal(type, NORMAL.getType());
    }

    public static boolean isSeckill(Integer type) {
        return ObjectUtil.equal(type, SECKILL.getType());
    }

    public static boolean isBargain(Integer type) {
        return ObjectUtil.equal(type, BARGAIN.getType());
    }

    public static boolean isCombination(Integer type) {
        return ObjectUtil.equal(type, COMBINATION.getType());
    }

    public static boolean isPoint(Integer type) {
        return ObjectUtil.equal(type, POINT.getType());
    }

}
