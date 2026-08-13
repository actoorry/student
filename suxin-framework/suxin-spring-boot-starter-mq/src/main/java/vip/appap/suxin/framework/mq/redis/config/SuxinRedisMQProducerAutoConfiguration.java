package vip.appap.suxin.framework.mq.redis.config;

import vip.appap.suxin.framework.mq.redis.core.RedisMQTemplate;
import vip.appap.suxin.framework.mq.redis.core.interceptor.RedisMessageInterceptor;
import vip.appap.suxin.framework.redis.config.SuxinRedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Redis 消息队列 Producer 配置类
 *
 * @author 书心软件
 */
@Slf4j
@AutoConfiguration(after = SuxinRedisAutoConfiguration.class)
public class SuxinRedisMQProducerAutoConfiguration {

    @Bean
    public RedisMQTemplate redisMQTemplate(StringRedisTemplate redisTemplate,
                                           List<RedisMessageInterceptor> interceptors) {
        RedisMQTemplate redisMQTemplate = new RedisMQTemplate(redisTemplate);
        // 添加拦截器
        interceptors.forEach(redisMQTemplate::addInterceptor);
        return redisMQTemplate;
    }

}
