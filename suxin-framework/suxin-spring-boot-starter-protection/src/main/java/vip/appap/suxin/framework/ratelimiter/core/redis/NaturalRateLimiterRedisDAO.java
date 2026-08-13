package vip.appap.suxin.framework.ratelimiter.core.redis;

import vip.appap.suxin.framework.ratelimiter.core.annotation.NaturalPeriod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * 自然时间段限流 Redis DAO
 *
 * 使用 Redis 的 INCR + EXPIRE 实现计数，支持按自然时间段限流。
 *
 * @author 书心软件
 */
@AllArgsConstructor
public class NaturalRateLimiterRedisDAO {

    /**
     * 限流 Key 格式：rate_limiter:natural:{methodKey}:{periodKey}
     *
     * 例如：rate_limiter:natural:partner_name_check_verify_user123:DAY:20260610
     */
    private static final String RATE_LIMITER_NATURAL = "rate_limiter:natural:%s:%s:%s";

    private final RedissonClient redissonClient;

    /**
     * 尝试获取许可
     *
     * @param methodKey 方法唯一标识
     * @param period 限流时间段类型
     * @param maxCount 最大允许次数
     * @return 限流结果
     */
    public NaturalRateLimitResult tryAcquire(String methodKey, NaturalPeriod period, int maxCount) {
        // 1. 生成 Redis Key
        String periodKey = period.currentKey();
        String redisKey = formatKey(methodKey, period.name(), periodKey);

        // 2. 原子递增
        RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
        long currentCount = atomicLong.incrementAndGet();

        // 3. 设置过期时间（仅首次设置）
        if (currentCount == 1) {
            Duration remaining = period.remainingUntilReset();
            // 多加 1 秒避免边界问题
            atomicLong.expire(remaining.plusSeconds(1));
        }

        // 4. 判断是否超限
        boolean allowed = currentCount <= maxCount;

        // 5. 计算剩余信息
        int remainingCount = allowed ? (int) (maxCount - currentCount) : 0;
        Duration remainingTime = period.remainingUntilReset();

        return new NaturalRateLimitResult(allowed, (int) currentCount, remainingCount, remainingTime, period);
    }

    /**
     * 查询当前已使用次数（不递增）
     *
     * @param methodKey 方法唯一标识
     * @param period 限流时间段类型
     * @return 当前已使用次数
     */
    public long getCurrentCount(String methodKey, NaturalPeriod period) {
        String periodKey = period.currentKey();
        String redisKey = formatKey(methodKey, period.name(), periodKey);

        RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
        return atomicLong.get();
    }

    private static String formatKey(String methodKey, String periodName, String periodKey) {
        return String.format(RATE_LIMITER_NATURAL, methodKey, periodName, periodKey);
    }

    /**
     * 限流结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaturalRateLimitResult {

        /**
         * 是否允许通过
         */
        private boolean allowed;

        /**
         * 当前已使用次数
         */
        private int currentCount;

        /**
         * 剩余可用次数
         */
        private int remainingCount;

        /**
         * 距离重置的剩余时间
         */
        private Duration remainingTime;

        /**
         * 限流时间段类型
         */
        private NaturalPeriod period;

        /**
         * 格式化剩余时间为用户友好的提示（精确到天）
         *
         * @return 格式化后的剩余时间，如 "今天"、"明天"、"2天后"
         */
        public String formatRemainingTime() {
            if (remainingTime == null) {
                return "";
            }

            long seconds = remainingTime.getSeconds();
            if (seconds <= 0) {
                return "今天";
            }

            long days = seconds / (24 * 60 * 60);
            if (days == 0) {
                return "今天";
            } else if (days == 1) {
                return "明天";
            } else {
                return days + "天后";
            }
        }

        /**
         * 获取默认的超限提示信息
         *
         * @return 超限提示
         */
        public String getDefaultMessage() {
            String timeHint = formatRemainingTime();
            if (timeHint.isEmpty()) {
                return "认证次数已达上限";
            }
            return "认证次数已达上限，请" + timeHint + "再试";
        }
    }
}
