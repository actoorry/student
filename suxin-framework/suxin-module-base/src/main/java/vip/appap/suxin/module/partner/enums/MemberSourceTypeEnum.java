package vip.appap.suxin.module.partner.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MemberSourceTypeEnum implements ArrayValuable<Integer> {

    ORDER(1, "订单购买"),
    ADMIN_GRANT(2, "后台赠送");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(MemberSourceTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
