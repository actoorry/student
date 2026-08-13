package vip.appap.suxin.module.accountant.dal.redis.account;

import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static vip.appap.suxin.module.accountant.dal.redis.RedisKeyConstants.ACCOUNT_LOCK;

/**
 * 支付钱包的锁 Redis DAO
 *
 * @author 书心软件
 */
@Repository
public class AccountLockRedisDAO {

    @Resource
    private RedissonClient redissonClient;

    public <V> V lock(Long id, Long timeoutMillis, Callable<V> callable) throws Exception {
        String lockKey = formatKey(id);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.lock(timeoutMillis, TimeUnit.MILLISECONDS);
            // 执行逻辑
            return callable.call();
        } catch (Exception e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }

    private static String formatKey(Long id) {
        return String.format(ACCOUNT_LOCK, id);
    }

}

