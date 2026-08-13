package vip.appap.suxin.module.partner.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 客户等级
 *
 * @author Wanwan
 */
@Getter
@AllArgsConstructor
public enum PartnerSalesLevelEnum implements ArrayValuable<Integer> {

    IMPORTANT(1, "A（重点客户）"),
    GENERAL(2, "B（普通客户）"),
    LOW_PRIORITY(3, "C（非优先客户）");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PartnerSalesLevelEnum::getLevel).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer level;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
