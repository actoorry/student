package vip.appap.suxin.framework.ratelimiter.core.annotation;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NaturalPeriod 单元测试
 *
 * @author 书心软件
 */
class NaturalPeriodTest {

    @Test
    void testCurrentKey() {
        // 测试各种时间段的 Key 生成
        String dayKey = NaturalPeriod.DAY.currentKey();
        assertNotNull(dayKey);
        assertEquals(8, dayKey.length()); // yyyyMMdd

        String monthKey = NaturalPeriod.MONTH.currentKey();
        assertNotNull(monthKey);
        assertEquals(6, monthKey.length()); // yyyyMM

        String hourKey = NaturalPeriod.HOUR.currentKey();
        assertNotNull(hourKey);
        assertEquals(10, hourKey.length()); // yyyyMMddHH

        String minuteKey = NaturalPeriod.MINUTE.currentKey();
        assertNotNull(minuteKey);
        assertEquals(12, minuteKey.length()); // yyyyMMddHHmm

        String yearKey = NaturalPeriod.YEAR.currentKey();
        assertNotNull(yearKey);
        assertEquals(4, yearKey.length()); // yyyy
    }

    @Test
    void testFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 10, 14, 30, 45);

        assertEquals("20260610", NaturalPeriod.DAY.format(dateTime));
        assertEquals("202606", NaturalPeriod.MONTH.format(dateTime));
        assertEquals("2026061014", NaturalPeriod.HOUR.format(dateTime));
        assertEquals("202606101430", NaturalPeriod.MINUTE.format(dateTime));
        assertEquals("2026", NaturalPeriod.YEAR.format(dateTime));
    }

    @Test
    void testRemainingUntilReset() {
        // 测试各种时间段的剩余时间
        Duration dayRemaining = NaturalPeriod.DAY.remainingUntilReset();
        assertNotNull(dayRemaining);
        assertTrue(dayRemaining.toHours() <= 24);
        assertTrue(dayRemaining.toHours() >= 0);

        Duration monthRemaining = NaturalPeriod.MONTH.remainingUntilReset();
        assertNotNull(monthRemaining);
        assertTrue(monthRemaining.toDays() <= 31);
        assertTrue(monthRemaining.toDays() >= 0);

        Duration hourRemaining = NaturalPeriod.HOUR.remainingUntilReset();
        assertNotNull(hourRemaining);
        assertTrue(hourRemaining.toMinutes() <= 60);
        assertTrue(hourRemaining.toMinutes() >= 0);
    }

    @Test
    void testGetDisplayName() {
        assertEquals("分钟", NaturalPeriod.MINUTE.getDisplayName());
        assertEquals("小时", NaturalPeriod.HOUR.getDisplayName());
        assertEquals("天", NaturalPeriod.DAY.getDisplayName());
        assertEquals("周", NaturalPeriod.WEEK.getDisplayName());
        assertEquals("月", NaturalPeriod.MONTH.getDisplayName());
        assertEquals("年", NaturalPeriod.YEAR.getDisplayName());
    }
}
