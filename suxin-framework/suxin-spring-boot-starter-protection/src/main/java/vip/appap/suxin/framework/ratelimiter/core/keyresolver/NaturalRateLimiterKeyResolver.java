package vip.appap.suxin.framework.ratelimiter.core.keyresolver;

import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalRateLimit;
import org.aspectj.lang.JoinPoint;

/**
 * 自然时间段限流 Key 解析器接口
 *
 * @author 书心软件
 */
public interface NaturalRateLimiterKeyResolver {

    /**
     * 解析一个 Key
     *
     * @param naturalRateLimit 限流注解
     * @param joinPoint AOP 切面
     * @return Key
     */
    String resolver(JoinPoint joinPoint, NaturalRateLimit naturalRateLimit);

}
