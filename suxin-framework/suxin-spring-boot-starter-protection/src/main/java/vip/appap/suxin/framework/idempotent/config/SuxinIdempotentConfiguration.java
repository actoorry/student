package vip.appap.suxin.framework.idempotent.config;

import vip.appap.suxin.framework.idempotent.core.aop.IdempotentAspect;
import vip.appap.suxin.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import vip.appap.suxin.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import vip.appap.suxin.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import vip.appap.suxin.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import vip.appap.suxin.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import vip.appap.suxin.framework.redis.config.SuxinRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = SuxinRedisAutoConfiguration.class)
public class SuxinIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
