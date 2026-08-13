package vip.appap.suxin.module.system.enums;

/**
 * System 校验相关的常量
 *
 * 目的：统一账号等校验规则，避免在多个 VO 中复制粘贴正则与长度。
 *
 * @author 书心软件
 */
public interface SystemValidationConstants {

    // ========== 用户账号 USERNAME ==========

    /**
     * 用户账号正则：仅支持字母和数字
     *
     * 支持短工号登录（如 208），不强制字母与数字同时出现。
     */
    String USERNAME_PATTERN = "^[a-zA-Z0-9]{2,30}$";

    int USERNAME_MIN = 2;
    int USERNAME_MAX = 30;

    String USERNAME_LENGTH_MESSAGE = "账号长度为 2-30 位";
    String USERNAME_PATTERN_MESSAGE = "账号格式为数字以及字母";

}
