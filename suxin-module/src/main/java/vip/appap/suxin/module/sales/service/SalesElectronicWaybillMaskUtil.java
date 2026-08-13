package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * 电子面单凭据脱敏工具
 * <p>
 * 原则：凭据（月结账号、密码、密钥）任何读取接口只返回掩码值；日志只记录掩码或脱敏摘要。
 */
@UtilityClass
public class SalesElectronicWaybillMaskUtil {

    /**
     * 掩码凭据：仅保留末 4 位，前缀用星号填充；空值或过短直接返回全部星号
     *
     * @param raw 原始值
     * @return 掩码值
     */
    public static String mask(String raw) {
        if (StrUtil.isBlank(raw)) {
            return null;
        }
        String value = raw.trim();
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }

    /**
     * 日志安全摘要：只保留末 4 位，用于日志中的区分度，避免泄露完整凭据
     */
    public static String logSafe(String raw) {
        String masked = mask(raw);
        return masked == null ? "null" : masked;
    }

}
