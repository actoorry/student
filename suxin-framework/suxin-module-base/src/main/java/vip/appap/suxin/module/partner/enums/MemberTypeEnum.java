package vip.appap.suxin.module.partner.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MemberTypeEnum implements ArrayValuable<Integer> {

    MARRIAGE_ADVANCED(1, "婚恋高级会员");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MemberTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
