package vip.appap.suxin.framework.ratelimiter.core.annotation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 自然时间段枚举
 *
 * @author 书心软件
 */
public enum NaturalPeriod {

    /**
     * 按分钟
     */
    MINUTE("yyyyMMddHHmm", ChronoUnit.MINUTES),

    /**
     * 按小时
     */
    HOUR("yyyyMMddHH", ChronoUnit.HOURS),

    /**
     * 按天
     */
    DAY("yyyyMMdd", ChronoUnit.DAYS),

    /**
     * 按周（ISO 周）
     */
    WEEK("YYYYww", ChronoUnit.WEEKS),

    /**
     * 按月
     */
    MONTH("yyyyMM", ChronoUnit.MONTHS),

    /**
     * 按年
     */
    YEAR("yyyy", ChronoUnit.YEARS);

    /**
     * 日期格式化模式
     */
    private final String pattern;

    /**
     * 时间单位
     */
    private final ChronoUnit unit;

    NaturalPeriod(String pattern, ChronoUnit unit) {
        this.pattern = pattern;
        this.unit = unit;
    }

    /**
     * 生成当前时间段的 Key
     *
     * @return 时间段 Key，如 "20260610"
     */
    public String currentKey() {
        return format(LocalDateTime.now());
    }

    /**
     * 格式化时间为时间段 Key
     *
     * @param dateTime 时间
     * @return 时间段 Key
     */
    public String format(LocalDateTime dateTime) {
        if (this == WEEK) {
            // ISO 周格式：YYYYww
            return dateTime.format(DateTimeFormatter.ofPattern("YYYY"))
                    + String.format("%02d", dateTime.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取到下一个时间段重置点的剩余时间
     *
     * @return 剩余时间
     */
    public Duration remainingUntilReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReset = switch (this) {
            case MINUTE -> now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
            case HOUR -> now.truncatedTo(ChronoUnit.HOURS).plusHours(1);
            case DAY -> now.toLocalDate().plusDays(1).atStartOfDay();
            case WEEK -> now.toLocalDate().with(java.time.DayOfWeek.MONDAY).plusWeeks(1).atStartOfDay();
            case MONTH -> now.toLocalDate().withDayOfMonth(1).plusMonths(1).atStartOfDay();
            case YEAR -> now.toLocalDate().withDayOfYear(1).plusYears(1).atStartOfDay();
        };
        return Duration.between(now, nextReset);
    }

    /**
     * 获取时间段的中文名称
     *
     * @return 中文名称
     */
    public String getDisplayName() {
        return switch (this) {
            case MINUTE -> "分钟";
            case HOUR -> "小时";
            case DAY -> "天";
            case WEEK -> "周";
            case MONTH -> "月";
            case YEAR -> "年";
        };
    }

    public String getPattern() {
        return pattern;
    }

    public ChronoUnit getUnit() {
        return unit;
    }
}
