package vip.appap.suxin.module.hr.dal.redis.no;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import vip.appap.suxin.module.hr.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 劳动合同编号 Redis DAO
 *
 * 对齐 OA flow_sequence id=2：模板 {date:yyyy-MM-dd}{num}，分隔符为空 → yyyyMMdd + 当日递增序号
 * 例：202603041115 = 20260304 + 1115
 */
@Repository
public class HrContractNoRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 预览下一个编号（不占用序号，供新增表单展示）
     */
    public String preview() {
        String datePrefix = todayPrefix();
        String key = RedisKeyConstants.CONTRACT_NO + datePrefix;
        String current = stringRedisTemplate.opsForValue().get(key);
        long next = current == null ? 1 : Long.parseLong(current) + 1;
        return datePrefix + next;
    }

    /**
     * 生成并占用下一个编号（保存时调用）
     */
    public String generate() {
        String datePrefix = todayPrefix();
        String key = RedisKeyConstants.CONTRACT_NO + datePrefix;
        Long no = stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, Duration.ofDays(2L));
        return datePrefix + no;
    }

    private static String todayPrefix() {
        return DateUtil.format(LocalDateTime.now(), DatePattern.PURE_DATE_PATTERN);
    }

}
