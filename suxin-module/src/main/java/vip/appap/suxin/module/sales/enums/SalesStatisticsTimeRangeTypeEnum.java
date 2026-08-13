package vip.appap.suxin.module.sales.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 时间范围类型的枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum SalesStatisticsTimeRangeTypeEnum implements ArrayValuable<Integer> {

    /**
     * 天
     */
    DAY(1),
    /**
     * 周
     */
    WEEK(7),
    /**
     * 月
     */
    MONTH(30),
    /**
     * 年
     */
    YEAR(365),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesStatisticsTimeRangeTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
