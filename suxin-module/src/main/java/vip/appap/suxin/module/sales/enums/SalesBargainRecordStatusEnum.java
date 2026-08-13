package vip.appap.suxin.module.sales.enums;

import vip.appap.suxin.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 砍价记录的状态枚举
 *
 * @author 书心软件
 */
@AllArgsConstructor
@Getter
public enum SalesBargainRecordStatusEnum implements ArrayValuable<Integer> {

    IN_PROGRESS(1, "砍价中"),
    SUCCESS(2, "砍价成功"),
    FAILED(3, "砍价失败"), // 活动到期时，会自动将到期的砍价全部设置为过期
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(SalesBargainRecordStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 值
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
