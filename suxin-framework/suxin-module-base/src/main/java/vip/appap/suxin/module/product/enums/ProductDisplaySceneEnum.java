package vip.appap.suxin.module.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品展示场景枚举
 *
 * 场景编码为固定值，不允许前端修改。其中 {@code matchmaker_service} 保持小写以兼容已部署的红娘小程序页面。
 */
@AllArgsConstructor
@Getter
public enum ProductDisplaySceneEnum {

    MEMBER_PAGE("MEMBER_PAGE", "会员套餐页面"),
    EMOTION_COURSE_PAGE("EMOTION_COURSE_PAGE", "情感课程页面"),
    OFFLINE_ACTIVITY_PAGE("OFFLINE_ACTIVITY_PAGE", "线下活动页面"),
    MATCHMAKER_SERVICE("matchmaker_service", "红娘服务页面");

    /**
     * 展示场景编码
     */
    private final String code;

    /**
     * 运营端展示名称
     */
    private final String label;

    private static final Map<String, ProductDisplaySceneEnum> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(ProductDisplaySceneEnum::getCode, Function.identity()));

    public static ProductDisplaySceneEnum fromCode(String code) {
        return CODE_MAP.get(code);
    }

    public static List<ProductDisplaySceneEnum> all() {
        return Arrays.asList(values());
    }

}
