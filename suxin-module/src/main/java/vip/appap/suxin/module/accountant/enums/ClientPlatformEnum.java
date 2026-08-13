package vip.appap.suxin.module.accountant.enums;

import java.util.Locale;

/**
 * 诊断性客户端平台上下文，不作为认证或租户身份。
 */
public enum ClientPlatformEnum {
    MP_WEIXIN,
    APP_ANDROID,
    APP_IOS,
    H5,
    UNKNOWN;

    public static ClientPlatformEnum normalize(String value) {
        if (value == null || value.isBlank() || value.length() > 32) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }

    public static String normalizeVersion(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.trim();
        return normalized.substring(0, Math.min(normalized.length(), 64));
    }
}
