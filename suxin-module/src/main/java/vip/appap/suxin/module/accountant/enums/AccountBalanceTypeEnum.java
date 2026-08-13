package vip.appap.suxin.module.accountant.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 钱包余额类型枚举
 *
 * @author 书心软件
 */
@AllArgsConstructor
@Getter
public enum AccountBalanceTypeEnum implements ArrayValuable<String> {

    /**
     * 充值余额 — 用户主动充值，可用于购物、可退款
     */
    RECHARGE("RECHARGE", "充值余额"),

    /**
     * 佣金余额 — 分销/推广佣金，有锁定期，可提现
     */
    COMMISSION("COMMISSION", "佣金余额"),

    /**
     * 赠金 — 活动赠送/签到奖励，有有效期，不可提现
     */
    GIFT("GIFT", "赠金"),

    /**
     * 保证金 — 商家缴纳，不可消费，退店退还
     */
    DEPOSIT("DEPOSIT", "保证金"),
    ;

    public static final String[] ARRAYS = Arrays.stream(values()).map(AccountBalanceTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型
     */
    private final String type;
    /**
     * 名称
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static AccountBalanceTypeEnum valueOfType(String type) {
        return Arrays.stream(values()).filter(item -> item.getType().equals(type)).findFirst().orElse(null);
    }

}
