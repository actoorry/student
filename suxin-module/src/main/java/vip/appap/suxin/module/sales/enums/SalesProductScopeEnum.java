package vip.appap.suxin.module.sales.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 营销的商品范围枚举
 *
 * @author 书心软件
 */
@Getter
@AllArgsConstructor
public enum SalesProductScopeEnum implements ArrayValuable<Integer> {

    ALL(1, "全部商品"),
    SPU(2, "指定商品"),
    CATEGORY(3, "指定品类");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesProductScopeEnum::getScope).toArray(Integer[]::new);

    /**
     * 范围值
     */
    private final Integer scope;
    /**
     * 范围名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isAll(Integer scope) {
        return Objects.equals(scope, ALL.scope);
    }

    public static boolean isSpu(Integer scope) {
        return Objects.equals(scope, SPU.scope);
    }

    public static boolean isCategory(Integer scope) {
        return Objects.equals(scope, CATEGORY.scope);
    }

}
