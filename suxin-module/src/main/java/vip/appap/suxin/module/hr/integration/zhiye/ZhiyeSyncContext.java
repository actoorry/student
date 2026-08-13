package vip.appap.suxin.module.hr.integration.zhiye;

import lombok.Builder;
import lombok.Data;

/**
 * 智业 HIS 人员同步上下文（占位符数据源）
 */
@Data
@Builder
public class ZhiyeSyncContext {

    private String messageId;
    private String messageCreateTime;
    private String healthcareProvider;
    private String technologyCodeSystemName;
    private String technologyDisplayName;
    private String idCard;
    private String name;
    private String nameCode;
    private String genderCode;
    private String genderDisplayName;
    private String birthDate;
    private String originCode;
    private String branchCode;
    private String departmentCode;
    private String departmentName;
    private String birthPlace;
    private String applyId;
    private String applyName;

}
