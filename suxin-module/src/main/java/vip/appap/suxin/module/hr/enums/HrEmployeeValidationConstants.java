package vip.appap.suxin.module.hr.enums;

/**
 * 员工档案字段校验常量（与 partner / hr_employee 表列长度对齐）
 */
public interface HrEmployeeValidationConstants {

    // ========== partner 基础字段 ==========

    int NAME_MAX = 100;
    String NAME_LENGTH_MESSAGE = "姓名长度不能超过 " + NAME_MAX + " 个字符";

    int ID_CARD_MAX = 100;
    String ID_CARD_LENGTH_MESSAGE = "身份证号长度不能超过 " + ID_CARD_MAX + " 个字符";

    int DETAIL_ADDRESS_MAX = 255;
    String DETAIL_ADDRESS_LENGTH_MESSAGE = "详细地址长度不能超过 " + DETAIL_ADDRESS_MAX + " 个字符";

    int REMARK_MAX = 500;
    String REMARK_LENGTH_MESSAGE = "备注长度不能超过 " + REMARK_MAX + " 个字符";

    // ========== hr_employee 扩展字段 ==========

    /**
     * 与系统登录账号长度一致；库表需执行 employee_no 扩列脚本至 VARCHAR(30)
     */
    int EMPLOYEE_NO_MAX = 30;
    String EMPLOYEE_NO_LENGTH_MESSAGE = "员工工号长度不能超过 " + EMPLOYEE_NO_MAX + " 个字符";
    String EMPLOYEE_NO_PATTERN = "^[a-zA-Z0-9]*$";
    String EMPLOYEE_NO_PATTERN_MESSAGE = "员工工号只能包含字母和数字";

    int EMPLOYEE_MOBILE_MAX = 30;
    String EMPLOYEE_MOBILE_LENGTH_MESSAGE = "员工手机号长度不能超过 " + EMPLOYEE_MOBILE_MAX + " 个字符";
    String EMPLOYEE_MOBILE_PATTERN = "^1[3-9]\\d{9}$";
    String EMPLOYEE_MOBILE_PATTERN_MESSAGE = "员工手机号格式不正确";

    int TEXT_100_MAX = 100;
    String DUTY_LENGTH_MESSAGE = "职务长度不能超过 " + TEXT_100_MAX + " 个字符";
    String FULL_TIME_EDUCATION_LENGTH_MESSAGE = "全日制学历长度不能超过 " + TEXT_100_MAX + " 个字符";
    String BANK_CARD_LENGTH_MESSAGE = "银行卡卡号长度不能超过 " + TEXT_100_MAX + " 个字符";
    String HOME_INFORMATION_LENGTH_MESSAGE = "家庭信息长度不能超过 " + TEXT_100_MAX + " 个字符";
    String SCHOOL_MAJOR_LENGTH_MESSAGE = "毕业院校及专业长度不能超过 " + TEXT_100_MAX + " 个字符";
    String NAME_ABBREVIATION_LENGTH_MESSAGE = "姓名简写长度不能超过 " + TEXT_100_MAX + " 个字符";

}
