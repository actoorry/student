package vip.appap.suxin.module.rongjh.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

/**
 * 戎集汇模块错误码
 */
public interface ErrorCodeConstants {

    ErrorCode HELP_NOT_EXISTS = new ErrorCode(1_030_001_000, "帮扶申请不存在");
    ErrorCode HELP_CANNOT_CANCEL = new ErrorCode(1_030_001_001, "仅待审核的帮扶申请可撤销");
    ErrorCode HELP_REASON_REQUIRED = new ErrorCode(1_030_001_002, "求助说明不能为空");
    ErrorCode HELP_STATUS_AUDIT_FAIL = new ErrorCode(1_030_001_003, "仅待审核的帮扶申请可审核");
    ErrorCode HELP_STATUS_REJECT_FAIL = new ErrorCode(1_030_001_004, "仅待审核的帮扶申请可驳回");
    ErrorCode HELP_ACTUAL_AMOUNT_REQUIRED = new ErrorCode(1_030_001_005, "批准金额不能为空");

    // ========== 战友会 1_030_003_000 ==========
    ErrorCode WARRIOR_NOT_EXISTS = new ErrorCode(1_030_003_000, "战友会身份不存在");
    ErrorCode WARRIOR_STATUS_AUDIT_FAIL = new ErrorCode(1_030_003_001, "仅待审核的申请可审核");
    ErrorCode WARRIOR_STATUS_REJECT_FAIL = new ErrorCode(1_030_003_002, "当前状态不可驳回");
    ErrorCode WARRIOR_STATUS_BLACKLIST_FAIL = new ErrorCode(1_030_003_003, "仅已通过的成员可拉黑");

}
