package vip.appap.suxin.module.partner.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

/**
 * Partner 错误码枚举类
 * <p>
 * partner 系统，使用 1-004-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 用户相关  1-004-001-000 ============
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1_004_001_000, "用户不存在");
    ErrorCode USER_MOBILE_NOT_EXISTS = new ErrorCode(1_004_001_001, "手机号未注册用户");
    ErrorCode USER_MOBILE_USED = new ErrorCode(1_004_001_002, "修改手机失败，该手机号({})已经被使用");
    ErrorCode USER_EMAIL_USED = new ErrorCode(1_004_001_004, "邮箱({})已经被使用");

    // ========== 会员标签 1-004-003-000 ==========
    ErrorCode PARTNER_TAG_NOT_EXISTS = new ErrorCode(1_004_003_000, "会员标签不存在");
    ErrorCode PARTNER_TAG_EXISTS = new ErrorCode(1_004_003_001, "会员标签已存在");

    // ========== 会员分组 1-004-004-000 ==========
    ErrorCode PARTNER_GROUP_NOT_EXISTS = new ErrorCode(1_004_004_000, "会员分组不存在");
    ErrorCode PARTNER_GROUP_EXISTS = new ErrorCode(1_004_004_001, "会员分组已存在");

    // ========== 会员等级 1-004-005-000 ==========
    ErrorCode PARTNER_LEVEL_NOT_EXISTS = new ErrorCode(1_004_005_000, "会员等级不存在");
    ErrorCode PARTNER_LEVEL_EXISTS = new ErrorCode(1_004_005_001, "会员等级已存在");
    ErrorCode PARTNER_LEVEL_HAS_MEMBER = new ErrorCode(1_004_005_002, "该等级下还有会员，无法删除");

    // ========== 会员积分 1-004-006-000 ==========
    ErrorCode PARTNER_POINT_NOT_ENOUGH = new ErrorCode(1_004_006_000, "积分不足");

    // ========== 会员签到 1-004-007-000 ==========
    ErrorCode PARTNER_SIGN_IN_CONFIG_NOT_EXISTS = new ErrorCode(1_004_007_000, "签到配置不存在");
    ErrorCode PARTNER_SIGN_IN_ALREADY = new ErrorCode(1_004_007_001, "今日已签到");

    // ========== 会员地址 1-004-008-000 ==========
    ErrorCode PARTNER_ADDRESS_NOT_EXISTS = new ErrorCode(1_004_008_000, "会员地址不存在");

    // ========== 会员认证 1-004-009-000 ==========
    ErrorCode PARTNER_USERNAME_EXISTS = new ErrorCode(1_004_009_000, "用户名已存在");
    ErrorCode PARTNER_MOBILE_EXISTS = new ErrorCode(1_004_009_001, "手机号已存在");
    ErrorCode PARTNER_USERNAME_PASSWORD_FAILED = new ErrorCode(1_004_009_002, "用户名或密码错误");
    ErrorCode PARTNER_MOBILE_PASSWORD_FAILED = new ErrorCode(1_004_009_003, "手机号或密码错误");
    ErrorCode AUTH_SOCIAL_USER_NOT_FOUND = new ErrorCode(1_004_009_004, "社交用户不存在");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1_004_009_005, "用户被禁用");
    ErrorCode AUTH_WX_LOGIN_FAIL = new ErrorCode(1_004_009_006, "微信登录失败");

    ErrorCode PARTNER_NOT_EXISTS = new ErrorCode(1_004_010_000, "会员不存在");
    ErrorCode PARTNER_IS_DISABLE = new ErrorCode(1_004_010_001, "合作伙伴已禁用");

    // ========== 付费会员 1-004-011-000 ==========
    ErrorCode MEMBER_NOT_EXISTS = new ErrorCode(1_004_011_000, "会员记录不存在");
    ErrorCode MEMBER_CONFIG_NOT_FOUND = new ErrorCode(1_004_011_001, "会员配置不存在");
    ErrorCode MEMBER_CONFIG_INVALID = new ErrorCode(1_004_011_002, "会员配置格式错误");
    ErrorCode MEMBER_DURATION_PARSE_FAILED = new ErrorCode(1_004_011_003, "会员时长解析失败");
    ErrorCode MEMBER_ORDER_DUPLICATED = new ErrorCode(1_004_011_004, "会员订单项已激活");
    ErrorCode MEMBER_GRANT_DURATION_INVALID = new ErrorCode(1_004_011_005, "会员赠送时长必须大于 0");

    // ========== 客户限制配置 1_004_012_000 ==========
    ErrorCode PARTNER_SALES_LIMIT_CONFIG_NOT_EXISTS = new ErrorCode(1_004_012_000, "客户限制配置不存在");

}
