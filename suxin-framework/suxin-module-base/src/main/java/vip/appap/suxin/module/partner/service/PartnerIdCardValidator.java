package vip.appap.suxin.module.partner.service;

import cn.hutool.core.util.StrUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

/**
 * 中国居民身份证号校验器。
 */
public final class PartnerIdCardValidator {

    private static final int ID_CARD_LENGTH = 18;

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    private static final DateTimeFormatter BIRTHDAY_FORMATTER = DateTimeFormatter
            .ofPattern("uuuuMMdd")
            .withResolverStyle(ResolverStyle.STRICT);

    private PartnerIdCardValidator() {
    }

    /**
     * 校验身份证号。
     *
     * @return 空字符串表示通过；非空表示失败原因
     */
    public static String validate(String idCard) {
        if (StrUtil.isBlank(idCard)) {
            return "身份证号不能为空";
        }
        String normalizedIdCard = StrUtil.trim(idCard).toUpperCase();
        if (normalizedIdCard.length() != ID_CARD_LENGTH) {
            return "身份证号格式不正确";
        }
        for (int i = 0; i < ID_CARD_LENGTH - 1; i++) {
            if (!Character.isDigit(normalizedIdCard.charAt(i))) {
                return "身份证号格式不正确";
            }
        }
        char lastChar = normalizedIdCard.charAt(ID_CARD_LENGTH - 1);
        if (!Character.isDigit(lastChar) && lastChar != 'X') {
            return "身份证号格式不正确";
        }
        if (!isValidBirthday(normalizedIdCard.substring(6, 14))) {
            return "身份证号出生日期不合法";
        }
        if (!isValidCheckCode(normalizedIdCard)) {
            return "身份证号校验码不正确";
        }
        return "";
    }

    private static boolean isValidBirthday(String birthday) {
        try {
            LocalDate.parse(birthday, BIRTHDAY_FORMATTER);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isValidCheckCode(String idCard) {
        int sum = 0;
        for (int i = 0; i < ID_CARD_LENGTH - 1; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        char expectedCheckCode = CHECK_CODES[sum % 11];
        return expectedCheckCode == idCard.charAt(ID_CARD_LENGTH - 1);
    }

}
