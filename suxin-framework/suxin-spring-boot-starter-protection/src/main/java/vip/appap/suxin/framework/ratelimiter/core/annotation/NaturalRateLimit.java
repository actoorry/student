package vip.appap.suxin.framework.ratelimiter.core.annotation;

import vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.NaturalRateLimiterKeyResolver;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.impl.UserNaturalRateLimiterKeyResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自然时间段限流注解
 *
 * 支持按自然时间段（分钟、小时、天、周、月、年）进行限流，
 * 适用于认证次数限制等需要按自然时间段统计的场景。
 *
 * @author 书心软件
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NaturalRateLimit {

    /**
     * 限流的时间段类型，默认为按天
     */
    NaturalPeriod period() default NaturalPeriod.DAY;

    /**
     * 限流次数（如果设置了 configKey，则优先从配置文件读取）
     */
    int count() default 10;

    /**
     * 配置文件中的 key，用于动态读取限流次数
     *
     * 例如：设置为 "suxin.rate-limit.real-name.day-count" 时，
     * 会从 application.yaml 中读取 suxin.rate-limit.real-name.day-count 配置值
     *
     * 如果配置文件中没有找到该配置，则使用 {@link #count()} 的默认值
     */
    String configKey() default "";

    /**
     * 提示信息，请求过于频繁的提示
     *
     * 为空时使用默认提示格式："请求过于频繁，请{剩余时间}后重试"
     *
     * @see GlobalErrorCodeConstants#TOO_MANY_REQUESTS
     */
    String message() default "";

    /**
     * 使用的 Key 解析器
     *
     * @see UserNaturalRateLimiterKeyResolver 默认按用户限流
     */
    Class<? extends NaturalRateLimiterKeyResolver> keyResolver() default UserNaturalRateLimiterKeyResolver.class;

    /**
     * 使用的 Key 参数
     */
    String keyArg() default "";

}
