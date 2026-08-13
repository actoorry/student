package vip.appap.suxin.module.partner.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MemberStatusEnum implements ArrayValuable<Integer> {

    ACTIVE(1, "有效"),
    EXPIRED(2, "已过期");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MemberStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
