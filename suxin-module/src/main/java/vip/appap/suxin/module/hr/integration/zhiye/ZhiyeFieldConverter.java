package vip.appap.suxin.module.hr.integration.zhiye;



import cn.hutool.core.util.StrUtil;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import vip.appap.suxin.framework.dict.core.DictFrameworkUtils;

import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeeRespVO;



import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;

import java.util.Map;

import java.util.UUID;



/**

 * suxin 员工字段 → 智业 SOAP 占位符转换（默认对齐旧 OA providerInfoRegisterIn）

 */

@Slf4j

@Component

@RequiredArgsConstructor

public class ZhiyeFieldConverter {



    private static final String DICT_HR_PERSONNEL_CATEGORY = "hr_personnel_category";



    /** OA work_class → 智业 1~6（与旧 OA FTapiServiceImpl 一致） */

    private static final Map<String, String> PERSONNEL_TO_ZHIYE_CODE = Map.of(

            "30", "1",

            "40", "2",

            "50", "3",

            "60", "4",

            "10", "5",

            "02", "6"

    );



    private static final Map<String, String> ZHIYE_CODE_TO_DISPLAY = Map.of(

            "1", "医生",

            "2", "护士",

            "3", "药剂",

            "4", "医技",

            "5", "行政",

            "6", "后勤"

    );



    private static final DateTimeFormatter HL7_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final DateTimeFormatter OA_BIRTH_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");



    private final ZhiyeHisProperties properties;



    public ZhiyeSyncContext buildContext(EmployeeRespVO employee, boolean register) {

        if (Boolean.TRUE.equals(properties.getOaCompatible())) {

            return buildOaContext(employee);

        }

        return buildExtendedContext(employee, register);

    }



    /**
     * 对齐旧 OA {@code providerInfoRegisterIn}：name/work_id/id_card/sex/born_date/work_type/dept
     */
    private ZhiyeSyncContext buildOaContext(EmployeeRespVO employee) {
        GenderMapping gender = toRegisterGender(employee.getSex());
        String workType = StrUtil.nullToEmpty(employee.getPersonnelCategory());
        String technologyDisplayName = toOaTechnologyDisplayName(workType);

        return ZhiyeSyncContext.builder()
                .name(StrUtil.nullToEmpty(employee.getName()))
                .healthcareProvider(StrUtil.nullToEmpty(employee.getEmployeeNo()))
                .idCard(StrUtil.nullToEmpty(employee.getIdCard()))
                .genderCode(gender.code())
                .genderDisplayName(gender.display())
                .birthDate(formatOaBirthDate(employee.getBirthday()))
                // OA 第 82 行：technologyCodeSystemName 直接用 work_type 原值（如 30/02），不做 1~6 映射
                .technologyCodeSystemName(workType)
                .technologyDisplayName(technologyDisplayName)
                .departmentName(resolveOaDepartmentName(employee))
                .build();
    }

    /**
     * OA 第 83~100 行：仅当 work_type 为 1~6 时填 displayName，work_class 编码（30 等）时 displayName 为空
     */
    private static String toOaTechnologyDisplayName(String workType) {
        if ("1".equals(workType)) {
            return "医生";
        }
        if ("2".equals(workType)) {
            return "护士";
        }
        if ("3".equals(workType)) {
            return "药剂";
        }
        if ("4".equals(workType)) {
            return "医技";
        }
        if ("5".equals(workType)) {
            return "行政";
        }
        if ("6".equals(workType)) {
            return "后勤";
        }
        return "";
    }



    private ZhiyeSyncContext buildExtendedContext(EmployeeRespVO employee, boolean register) {

        GenderMapping gender = register

                ? toRegisterGender(employee.getSex())

                : toUpdateGender(employee.getSex());

        CategoryMapping category = toPersonnelCategory(employee.getPersonnelCategory());



        String messageId = UUID.randomUUID().toString().replace("-", "");

        String birthDate = formatHl7BirthDate(employee.getBirthday());

        String deptCode = employee.getDept() != null ? String.valueOf(employee.getDept()) : "";



        return ZhiyeSyncContext.builder()

                .messageId(messageId)

                .messageCreateTime(LocalDateTime.now().format(HL7_DATETIME))

                .healthcareProvider(StrUtil.nullToEmpty(employee.getEmployeeNo()))

                .technologyCodeSystemName(category.code())

                .technologyDisplayName(category.display())

                .idCard(StrUtil.nullToEmpty(employee.getIdCard()))

                .name(StrUtil.nullToEmpty(employee.getName()))

                .nameCode(StrUtil.nullToEmpty(employee.getNameAbbreviation()))

                .genderCode(gender.code())

                .genderDisplayName(gender.display())

                .birthDate(birthDate)

                .originCode(StrUtil.nullToEmpty(properties.getOriginCode()))

                .branchCode(StrUtil.nullToEmpty(properties.getBranchCode()))

                .departmentCode(deptCode)

                .departmentName(StrUtil.nullToEmpty(employee.getDeptName()))

                .birthPlace(StrUtil.nullToEmpty(properties.getBirthPlace()))

                .applyId(StrUtil.nullToEmpty(properties.getApplyId()))

                .applyName("系统管理员")

                .build();

    }



    /** 智业 {@code @departmentName@}：直接推 {@code hr_employee.dept}（= system_dept.id） */
    private static String resolveOaDepartmentName(EmployeeRespVO employee) {
        return employee.getDept() != null ? String.valueOf(employee.getDept()) : "";
    }



    private static String formatOaBirthDate(LocalDateTime birthday) {

        if (birthday == null) {

            return "";

        }

        return birthday.toLocalDate().format(OA_BIRTH_DATE);

    }



    private static String formatHl7BirthDate(LocalDateTime birthday) {

        if (birthday == null) {

            return "";

        }

        return birthday.toLocalDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    }



    /**

     * Register：芋道 SexEnum 1男/2女/0未知 → 智业 1/2/3（旧 OA providerInfoRegisterIn）

     */

    private static GenderMapping toRegisterGender(Integer sex) {

        if (sex == null || sex == 0) {

            return new GenderMapping("3", "不清楚性别");

        }

        if (sex == 1) {

            return new GenderMapping("1", "男");

        }

        if (sex == 2) {

            return new GenderMapping("2", "女");

        }

        return new GenderMapping("3", "不清楚性别");

    }



    /**

     * Update：芋道 → 智业 0/1/2（旧 OA providerInfoUpdate）

     */

    private static GenderMapping toUpdateGender(Integer sex) {

        if (sex == null || sex == 0) {

            return new GenderMapping("2", "不清楚性别");

        }

        if (sex == 1) {

            return new GenderMapping("0", "男");

        }

        if (sex == 2) {

            return new GenderMapping("1", "女");

        }

        return new GenderMapping("2", "不清楚性别");

    }



    private static CategoryMapping toPersonnelCategory(String personnelCategory) {

        if (StrUtil.isBlank(personnelCategory)) {

            return new CategoryMapping("5", "行政");

        }

        String zhiyeCode = PERSONNEL_TO_ZHIYE_CODE.getOrDefault(personnelCategory, personnelCategory);

        String display = ZHIYE_CODE_TO_DISPLAY.get(zhiyeCode);

        if (display == null) {

            display = DictFrameworkUtils.parseDictDataLabel(DICT_HR_PERSONNEL_CATEGORY, personnelCategory);

            if (StrUtil.isBlank(display)) {

                display = personnelCategory;

            }

        }

        return new CategoryMapping(zhiyeCode, display);

    }



    private record GenderMapping(String code, String display) {

    }



    private record CategoryMapping(String code, String display) {

    }



}

