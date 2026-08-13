package vip.appap.suxin.module.hr.enums;

import vip.appap.suxin.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    // ========== 员工 1-008-001-000 ==========
    ErrorCode EMPLOYEE_NOT_EXISTS = new ErrorCode(1_008_001_000, "员工不存在");
    ErrorCode EMPLOYEE_NO_DUPLICATE = new ErrorCode(1_008_001_001, "员工工号已存在");

    // ========== 薪资 1-008-002-000 ==========
    ErrorCode SALARY_NOT_EXISTS = new ErrorCode(1_008_002_000, "薪资不存在");
    ErrorCode SALARY_IMPORT_FILENAME_INVALID = new ErrorCode(1_008_002_001, "文件名无法识别年月，请使用「YYYY年MM月」格式，如「2025年12月份工资数据.xls」");
    ErrorCode SALARY_IMPORT_LIST_IS_EMPTY = new ErrorCode(1_008_002_002, "导入的薪资数据为空");
    ErrorCode SALARY_IMPORT_HEADER_INVALID = new ErrorCode(1_008_002_003, "Excel 表头校验失败，缺失列：{}");
    ErrorCode SALARY_IMPORT_DUPLICATE_NEED_CONFIRM = new ErrorCode(1_008_002_004, "存在 {} 条同年月同工号的薪资记录，确认后将覆盖");

    // ========== 劳动合同 1-008-003-000 ==========
    ErrorCode CONTRACT_NOT_EXISTS = new ErrorCode(1_008_003_000, "劳动合同不存在");

    // ========== 履历 1-008-004-000 ==========
    ErrorCode RESUME_NOT_EXISTS = new ErrorCode(1_008_004_000, "履历不存在");

    // ========== 证书 1-008-005-000 ==========
    ErrorCode CERTIFICATE_NOT_EXISTS = new ErrorCode(1_008_005_000, "证书不存在");
    ErrorCode CERTIFICATE_EMPLOYEE_NOT_EXISTS = new ErrorCode(1_008_005_001, "关联员工不存在");
    ErrorCode CERTIFICATE_NOTIFY_LOG_NOT_EXISTS = new ErrorCode(1_008_005_002, "推送日志不存在");
    ErrorCode CERTIFICATE_NOTIFY_LOG_NOT_RETRYABLE = new ErrorCode(1_008_005_003, "仅失败状态的推送日志可补推");

    // ========== 外出管理 1-008-006-000 ==========
    ErrorCode OUTBOUND_NOT_EXISTS = new ErrorCode(1_008_006_000, "外出记录不存在");

    // ========== 加班登记 1-008-007-000 ==========
    ErrorCode OVERTIME_NOT_EXISTS = new ErrorCode(1_008_007_000, "加班记录不存在");
    ErrorCode OVERTIME_EMPLOYEE_NOT_EXISTS = new ErrorCode(1_008_007_001, "关联员工不存在");
    ErrorCode OVERTIME_TIME_INVALID = new ErrorCode(1_008_007_002, "加班结束时间必须晚于开始时间");
    ErrorCode OVERTIME_HOLIDAY_NAME_REQUIRED = new ErrorCode(1_008_007_003, "节假日值班必须填写节假日名称");

    // ========== 职工自助「我的」 1-008-008-000 ==========
    ErrorCode HR_MY_EMPLOYEE_NOT_BOUND = new ErrorCode(1_008_008_000, "当前账号未关联人事档案，无法使用自助功能");
    ErrorCode HR_MY_DATA_NOT_YOURS = new ErrorCode(1_008_008_001, "无权操作他人数据");

    // ========== 岗位聘任 1-008-009-000 ==========
    ErrorCode APPOINTMENT_NOT_EXISTS = new ErrorCode(1_008_009_000, "岗位聘任记录不存在");
    ErrorCode APPOINTMENT_EMPLOYEE_NOT_EXISTS = new ErrorCode(1_008_009_001, "关联员工不存在");

    // ========== 岗位异动 1-008-010-000 ==========
    ErrorCode POSITION_CHANGE_NOT_EXISTS = new ErrorCode(1_008_010_000, "岗位异动记录不存在");
    ErrorCode POSITION_CHANGE_EMPLOYEE_NOT_EXISTS = new ErrorCode(1_008_010_001, "关联员工不存在");

    // ========== 智业 HIS 同步 1-008-011-000 ==========
    ErrorCode ZHIYE_SYNC_FAILED = new ErrorCode(1_008_011_000, "智业 HIS 同步失败：{}");

}
