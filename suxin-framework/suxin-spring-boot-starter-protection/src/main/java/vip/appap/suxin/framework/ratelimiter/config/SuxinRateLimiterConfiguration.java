package vip.appap.suxin.framework.ratelimiter.config;

import vip.appap.suxin.framework.ratelimiter.core.aop.NaturalRateLimiterAspect;
import vip.appap.suxin.framework.ratelimiter.core.aop.RateLimiterAspect;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.NaturalRateLimiterKeyResolver;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.RateLimiterKeyResolver;
import vip.appap.suxin.framework.ratelimiter.core.keyresolver.impl.*;
import vip.appap.suxin.framework.ratelimiter.core.redis.NaturalRateLimiterRedisDAO;
import vip.appap.suxin.framework.ratelimiter.core.redis.RateLimiterRedisDAO;
import vip.appap.suxin.framework.redis.config.SuxinRedisAutoConfiguration;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.List;

@AutoConfiguration(after = SuxinRedisAutoConfiguration.class)
public class SuxinRateLimiterConfiguration {

    @Bean
    public RateLimiterAspect rateLimiterAspect(List<RateLimiterKeyResolver> keyResolvers, RateLimiterRedisDAO rateLimiterRedisDAO) {
        return new RateLimiterAspect(keyResolvers, rateLimiterRedisDAO);
    }

    @Bean
    public NaturalRateLimiterAspect naturalRateLimiterAspect(List<NaturalRateLimiterKeyResolver> keyResolvers,
                                                              NaturalRateLimiterRedisDAO naturalRateLimiterRedisDAO,
                                                              Environment environment) {
        return new NaturalRateLimiterAspect(keyResolvers, naturalRateLimiterRedisDAO, environment);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public RateLimiterRedisDAO rateLimiterRedisDAO(RedissonClient redissonClient) {
        return new RateLimiterRedisDAO(redissonClient);
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public NaturalRateLimiterRedisDAO naturalRateLimiterRedisDAO(RedissonClient redissonClient) {
        return new NaturalRateLimiterRedisDAO(redissonClient);
    }

    // ========== 各种 RateLimiterKeyResolver Bean ==========

    @Bean
    public DefaultRateLimiterKeyResolver defaultRateLimiterKeyResolver() {
        return new DefaultRateLimiterKeyResolver();
    }

    @Bean
    public UserRateLimiterKeyResolver userRateLimiterKeyResolver() {
        return new UserRateLimiterKeyResolver();
    }

    @Bean
    public ClientIpRateLimiterKeyResolver clientIpRateLimiterKeyResolver() {
        return new ClientIpRateLimiterKeyResolver();
    }

    @Bean
    public ServerNodeRateLimiterKeyResolver serverNodeRateLimiterKeyResolver() {
        return new ServerNodeRateLimiterKeyResolver();
    }

    @Bean
    public ExpressionRateLimiterKeyResolver expressionRateLimiterKeyResolver() {
        return new ExpressionRateLimiterKeyResolver();
    }

    // ========== 自然时间段限流 KeyResolver Bean ==========

    @Bean
    public UserNaturalRateLimiterKeyResolver userNaturalRateLimiterKeyResolver() {
        return new UserNaturalRateLimiterKeyResolver();
    }

}
