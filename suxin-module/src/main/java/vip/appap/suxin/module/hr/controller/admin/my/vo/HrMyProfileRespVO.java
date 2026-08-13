package vip.appap.suxin.module.hr.controller.admin.my.vo;

import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeeRespVO;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumeRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 职工「个人档案」只读聚合 Response VO
 *
 * 仅用于 /hr/my/profile/get，不可编辑。聚合员工花名册 + 证书列表 + 履历列表。
 *
 * @author suxin
 */
@Schema(description = "管理后台 - 职工个人档案 Response VO")
@Data
public class HrMyProfileRespVO {

    @Schema(description = "基本信息（花名册）")
    private EmployeeRespVO employee;

    @Schema(description = "执业证书列表")
    private List<CertificateRespVO> certificates;

    @Schema(description = "工作履历列表")
    private List<ResumeRespVO> resumes;

}
