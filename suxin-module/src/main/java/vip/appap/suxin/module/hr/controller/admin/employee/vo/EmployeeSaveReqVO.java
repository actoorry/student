package vip.appap.suxin.module.hr.controller.admin.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

import static vip.appap.suxin.module.hr.enums.HrEmployeeValidationConstants.*;

@Schema(description = "管理后台 - 员工新增/修改 Request VO")
@Data
public class EmployeeSaveReqVO {

    @Schema(description = "主键", example = "14241")
    private Long id;

    // ========== partner 基础信息（双写 partner 表） ==========
    @Schema(description = "关联 partner.id（新增时为空，编辑时必填）", example = "30316")
    private Long partnerId;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Size(max = NAME_MAX, message = NAME_LENGTH_MESSAGE)
    private String name;

    @Schema(description = "身份证号", example = "340421199001011234")
    @Size(max = ID_CARD_MAX, message = ID_CARD_LENGTH_MESSAGE)
    private String idCard;

    @Schema(description = "性别（0 男 1 女）", example = "0")
    private Integer sex;

    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDateTime birthday;

    @Schema(description = "头像", example = "https://xxx/avatar.png")
    private String avatar;

    @Schema(description = "详细地址", example = "安徽省凤台县xxx")
    @Size(max = DETAIL_ADDRESS_MAX, message = DETAIL_ADDRESS_LENGTH_MESSAGE)
    private String detailAddress;

    @Schema(description = "备注")
    @Size(max = REMARK_MAX, message = REMARK_LENGTH_MESSAGE)
    private String remark;

    // ========== hr_employee 扩展信息 ==========
    @Schema(description = "员工工号")
    @Size(max = EMPLOYEE_NO_MAX, message = EMPLOYEE_NO_LENGTH_MESSAGE)
    @Pattern(regexp = EMPLOYEE_NO_PATTERN, message = EMPLOYEE_NO_PATTERN_MESSAGE)
    private String employeeNo;

    @Schema(description = "员工手机号（人事专用）", example = "13800138000")
    @Size(max = EMPLOYEE_MOBILE_MAX, message = EMPLOYEE_MOBILE_LENGTH_MESSAGE)
    @Pattern(regexp = "^(|1[3-9]\\d{9})$", message = EMPLOYEE_MOBILE_PATTERN_MESSAGE)
    private String employeeMobile;

    @Schema(description = "所在部门（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "岗位编号，关联 system_post.id")
    private Long postId;

    @Schema(description = "人员类别")
    @Size(max = TEXT_100_MAX, message = "人员类别长度不能超过 100 个字符")
    private String personnelCategory;

    @Schema(description = "职称")
    @Size(max = TEXT_100_MAX, message = "职称长度不能超过 100 个字符")
    private String professionalTitle;

    @Schema(description = "政治面貌", example = "1")
    @Size(max = TEXT_100_MAX, message = "政治面貌长度不能超过 100 个字符")
    private String politicalStatus;

    @Schema(description = "入党时间")
    private LocalDateTime partyJoinDate;

    @Schema(description = "参加工作时间")
    private LocalDateTime careerStartDate;

    @Schema(description = "进入单位时间")
    private LocalDateTime hireDate;

    @Schema(description = "岗位（冗余，由 postId 同步）")
    @Size(max = TEXT_100_MAX, message = "岗位长度不能超过 100 个字符")
    private String position;

    @Schema(description = "职务")
    @Size(max = TEXT_100_MAX, message = DUTY_LENGTH_MESSAGE)
    private String duty;

    @Schema(description = "任职时间")
    private LocalDateTime appointmentDate;

    @Schema(description = "最高学历")
    @Size(max = TEXT_100_MAX, message = "最高学历长度不能超过 100 个字符")
    private String highestEducation;

    @Schema(description = "全日制学历")
    @Size(max = TEXT_100_MAX, message = FULL_TIME_EDUCATION_LENGTH_MESSAGE)
    private String fullTimeEducation;

    @Schema(description = "是否在编", example = "1")
    @Size(max = TEXT_100_MAX, message = "是否在编长度不能超过 100 个字符")
    private String establishmentStatus;

    @Schema(description = "人员身份")
    @Size(max = TEXT_100_MAX, message = "人员身份长度不能超过 100 个字符")
    private String personnelIdentity;

    @Schema(description = "人员来源")
    @Size(max = TEXT_100_MAX, message = "人员来源长度不能超过 100 个字符")
    private String recruitmentSource;

    @Schema(description = "进入形式")
    @Size(max = TEXT_100_MAX, message = "进入形式长度不能超过 100 个字符")
    private String entryMode;

    @Schema(description = "在职状态", example = "1")
    @Size(max = TEXT_100_MAX, message = "在职状态长度不能超过 100 个字符")
    private String employmentStatus;

    @Schema(description = "民族")
    @Size(max = TEXT_100_MAX, message = "民族长度不能超过 100 个字符")
    private String ethnicity;

    @Schema(description = "籍贯-省")
    @Size(max = TEXT_100_MAX, message = "籍贯长度不能超过 100 个字符")
    private String nativeProvince;

    @Schema(description = "籍贯-市")
    @Size(max = TEXT_100_MAX, message = "籍贯长度不能超过 100 个字符")
    private String nativeCity;

    @Schema(description = "银行卡卡号")
    @Size(max = TEXT_100_MAX, message = BANK_CARD_LENGTH_MESSAGE)
    private String bankCard;

    @Schema(description = "家庭信息")
    @Size(max = TEXT_100_MAX, message = HOME_INFORMATION_LENGTH_MESSAGE)
    private String homeInformation;

    @Schema(description = "毕业院校及专业")
    @Size(max = TEXT_100_MAX, message = SCHOOL_MAJOR_LENGTH_MESSAGE)
    private String schoolMajor;

    @Schema(description = "职称医护（图表辅助）")
    @Size(max = TEXT_100_MAX, message = "职称医护长度不能超过 100 个字符")
    private String professionalCategory;

    @Schema(description = "姓名简写")
    @Size(max = TEXT_100_MAX, message = NAME_ABBREVIATION_LENGTH_MESSAGE)
    private String nameAbbreviation;

}
