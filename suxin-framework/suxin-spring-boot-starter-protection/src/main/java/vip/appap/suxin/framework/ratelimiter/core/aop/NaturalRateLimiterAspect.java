package vip.appap.suxin.framework.ratelimiter.core.aop;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.exception.ServiceException;
import vip.appap.suxin.framework.common.exception.enums.GlobalErrorCodeConstants;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalRateLimit;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.NaturalRateLimiterKeyResolver;
import vip.appap.suxin.framework.ratelimiter.core.redis.NaturalRateLimiterRedisDAO;
import vip.appap.suxin.framework.ratelimiter.core.redis.NaturalRateLimiterRedisDAO.NaturalRateLimitResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * 拦截声明了 {@link NaturalRateLimit} 注解的方法，实现自然时间段限流操作
 *
 * @author 书心软件
 */
@Aspect
@Slf4j
public class NaturalRateLimiterAspect {

    /**
     * NaturalRateLimiterKeyResolver 集合
     */
    private final Map<Class<? extends NaturalRateLimiterKeyResolver>, NaturalRateLimiterKeyResolver> keyResolvers;

    private final NaturalRateLimiterRedisDAO naturalRateLimiterRedisDAO;

    private final Environment environment;

    public NaturalRateLimiterAspect(List<NaturalRateLimiterKeyResolver> keyResolvers,
                                    NaturalRateLimiterRedisDAO naturalRateLimiterRedisDAO,
                                    Environment environment) {
        this.keyResolvers = CollectionUtils.convertMap(keyResolvers, NaturalRateLimiterKeyResolver::getClass);
        this.naturalRateLimiterRedisDAO = naturalRateLimiterRedisDAO;
        this.environment = environment;
    }

    @Before("@annotation(naturalRateLimit)")
    public void beforePointCut(JoinPoint joinPoint, NaturalRateLimit naturalRateLimit) {
        // 1. 获取限流次数（优先从配置文件读取）
        int maxCount = resolveMaxCount(naturalRateLimit);

        // 2. 获得 NaturalRateLimiterKeyResolver 对象
        NaturalRateLimiterKeyResolver keyResolver = keyResolvers.get(naturalRateLimit.keyResolver());
        Assert.notNull(keyResolver, "找不到对应的 NaturalRateLimiterKeyResolver");

        // 3. 解析方法 Key（包含用户信息）
        String methodKey = keyResolver.resolver(joinPoint, naturalRateLimit);

        // 4. 执行限流判断
        NaturalRateLimitResult result = naturalRateLimiterRedisDAO.tryAcquire(
                methodKey, naturalRateLimit.period(), maxCount);

        // 5. 如果超限，抛出异常
        if (!result.isAllowed()) {
            log.info("[beforePointCut][方法({}) 参数({}) 请求过于频繁，已使用 {}/{} 次，剩余 {}]",
                    joinPoint.getSignature().toString(),
                    joinPoint.getArgs(),
                    result.getCurrentCount(),
                    maxCount,
                    result.formatRemainingTime());

            // 获取提示信息
            String message = resolveMessage(naturalRateLimit, result);
            throw new ServiceException(GlobalErrorCodeConstants.TOO_MANY_REQUESTS.getCode(), message);
        }

        // 6. 记录日志（调试用）
        log.debug("[beforePointCut][方法({}) 限流通过，已使用 {}/{} 次，剩余 {} 次]",
                joinPoint.getSignature().toString(),
                result.getCurrentCount(),
                maxCount,
                result.getRemainingCount());
    }

    /**
     * 解析限流次数
     *
     * 优先级：配置文件 > 注解默认值
     *
     * @param naturalRateLimit 注解
     * @return 限流次数
     */
    private int resolveMaxCount(NaturalRateLimit naturalRateLimit) {
        // 如果设置了 configKey，尝试从配置文件读取
        String configKey = naturalRateLimit.configKey();
        if (StrUtil.isNotBlank(configKey)) {
            String configValue = environment.getProperty(configKey);
            if (StrUtil.isNotBlank(configValue)) {
                try {
                    return Integer.parseInt(configValue);
                } catch (NumberFormatException e) {
                    log.warn("[resolveMaxCount][配置 key={} 的值 {} 不是有效数字，使用默认值 {}]",
                            configKey, configValue, naturalRateLimit.count());
                }
            }
        }

        // 使用注解默认值
        return naturalRateLimit.count();
    }

    /**
     * 解析提示信息
     *
     * @param naturalRateLimit 注解
     * @param result 限流结果
     * @return 提示信息
     */
    private String resolveMessage(NaturalRateLimit naturalRateLimit, NaturalRateLimitResult result) {
        // 如果注解设置了自定义消息，使用自定义消息
        String message = naturalRateLimit.message();
        if (StrUtil.isNotBlank(message)) {
            return message;
        }

        // 使用默认消息格式
        return result.getDefaultMessage();
    }
}
