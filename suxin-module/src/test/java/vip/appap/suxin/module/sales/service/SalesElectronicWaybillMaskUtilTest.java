package vip.appap.suxin.module.sales.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 电子面单凭据脱敏测试
 */
class SalesElectronicWaybillMaskUtilTest {

    @Test
    void mask_nullReturnsNull() {
        assertNull(SalesElectronicWaybillMaskUtil.mask(null));
        assertNull(SalesElectronicWaybillMaskUtil.mask("   "));
    }

    @Test
    void mask_shortValueReturnsAllStars() {
        assertEquals("****", SalesElectronicWaybillMaskUtil.mask("ab"));
        assertEquals("****", SalesElectronicWaybillMaskUtil.mask("abcd"));
    }

    @Test
    void mask_longValueKeepsLast4() {
        assertEquals("****6789", SalesElectronicWaybillMaskUtil.mask("abc1236789"));
    }

    @Test
    void logSafe_nullReturnsLiteralNull() {
        assertEquals("null", SalesElectronicWaybillMaskUtil.logSafe(null));
    }

}
