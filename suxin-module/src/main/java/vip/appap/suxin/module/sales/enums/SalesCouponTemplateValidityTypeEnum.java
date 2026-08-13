package vip.appap.suxin.module.sales.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 优惠劵模板的有限期类型的枚举
 *
 * @author 书心软件
 */
@AllArgsConstructor
@Getter
public enum SalesCouponTemplateValidityTypeEnum implements ArrayValuable<Integer> {

    DATE(1, "固定日期"),
    TERM(2, "领取之后"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesCouponTemplateValidityTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 值
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
