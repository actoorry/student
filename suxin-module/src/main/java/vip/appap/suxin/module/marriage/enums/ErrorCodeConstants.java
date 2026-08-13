package vip.appap.suxin.module.marriage.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode MARRIAGE_AUTH_MEMBER_NOT_EXISTS = new ErrorCode(1002010002, "会员不存在");
    ErrorCode MARRIAGE_PROFILE_REAL_NAME_LOCKED = new ErrorCode(1002010011, "实名认证后性别和生日不可修改");
    ErrorCode MARRIAGE_FOLLOW_USER_NOT_EXISTS = new ErrorCode(1002010003, "当前用户不存在");
    ErrorCode MARRIAGE_FOLLOW_PARTNER_NOT_EXISTS = new ErrorCode(1002010004, "被关注用户不存在");
    ErrorCode MARRIAGE_FOLLOW_SELF_NOT_ALLOWED = new ErrorCode(1002010005, "不能关注自己");

    ErrorCode MARRIAGE_MOMENT_NOT_EXISTS = new ErrorCode(1002010006, "动态不存在");
    ErrorCode MARRIAGE_MOMENT_CONTENT_EMPTY = new ErrorCode(1002010007, "动态内容和图片不能同时为空");
    ErrorCode MARRIAGE_MOMENT_IMAGE_TOO_MANY = new ErrorCode(1002010008, "动态图片最多 9 张");
    ErrorCode MARRIAGE_MOMENT_COMMENT_NOT_EXISTS = new ErrorCode(1002010009, "评论不存在");
    ErrorCode MARRIAGE_MOMENT_DELETE_FORBIDDEN = new ErrorCode(1002010010, "只能删除自己的动态");

}
