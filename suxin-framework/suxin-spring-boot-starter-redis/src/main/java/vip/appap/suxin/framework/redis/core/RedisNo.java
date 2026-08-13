package vip.appap.suxin.framework.redis.core;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 订单序列号 Redis DAO
 *
 * <p>使用 Redis INCR 原子自增保证分布式环境下的唯一性，无需加锁。</p>
 *
 * <p>格式：yyyyMMddHHmmss（14 位）+ 5 位自增流水号 = 19 位</p>
 *
 * <p>示例：2026072214303300001</p>
 *
 * @author HUIHUI
 */
@Repository
public class RedisNo {

    /**
     * 流水号长度
     */
    private static final int SEQ_LENGTH = 5;

    /**
     * 流水号最大值（5 位 = 99999）
     */
    private static final long SEQ_MAX = 99999L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成唯一订单序号
     */
    public long getOrderNo() {
        // 1. 按秒建 key，同一秒内的序号共享一个计数器
        String keyPrefix = "OrderSalesNo";
        return getSerialNo(keyPrefix);
    }

    /**
     * 生成唯一订单序号
     * <p>每秒最多 99999 个序号，超出会重置为 1（同一秒内极少达到）。</p>
     * @return 19 位订单序号
     */
    public long getSerialNo(String keyPrefix) {
        if (StrUtil.isEmpty(keyPrefix)) {
            keyPrefix = "DefaultSalesNo";
        }

        // 1. 按秒建 key，同一秒内的序号共享一个计数器
        String key = LocalDateTime.now().format(DatePattern.PURE_DATETIME_FORMATTER);

        // 2. Redis INCR 原子自增
        Long seq = stringRedisTemplate.opsForValue().increment(keyPrefix + key);

        // 3. 首次创建时设 2 秒过期（1 秒后 key 过期，不影响下一秒的新 key）
        if (seq != null && seq == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(2));
        }

        // 4. 超出 5 位上限时重置（极端情况兜底）
        long safeSeq = (seq != null && seq <= SEQ_MAX) ? seq : (seq % SEQ_MAX + 1);

        // 5. 拼接：日期（14 位）+ 流水号（5 位）= 19 位
        key = key.substring(key.length() - 14) + String.format("%0" + SEQ_LENGTH + "d", safeSeq);

        return Long.parseLong(key);
    }
}
