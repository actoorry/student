package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificatePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.controller.admin.employee.vo.EmployeeRespVO;
import vip.appap.suxin.module.hr.controller.admin.my.vo.HrMyProfileRespVO;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.resume.vo.ResumeRespVO;
import vip.appap.suxin.module.hr.dal.mysql.employee.EmployeeMapper;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import vip.appap.suxin.module.hr.service.certificate.CertificateService;
import vip.appap.suxin.module.hr.service.resume.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * 职工「个人档案」只读 Controller
 *
 * 路径 /hr/my/profile，权限 hr:my:profile:query。
 * 仅提供 GET /get，禁止任何写操作。partnerId 由后端强制写入，忽略前端入参。
 *
 * @author suxin
 */
@Tag(name = "管理后台 - 职工个人档案")
@RestController
@RequestMapping("/hr/my/profile")
@Validated
public class HrMyProfileController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private CertificateService certificateService;
    @Resource
    private ResumeService resumeService;

    @GetMapping("/get")
    @Operation(summary = "获得我的个人档案（只读聚合）")
    @PreAuthorize("@ss.hasPermission('hr:my:profile:query')")
    public CommonResult<HrMyProfileRespVO> getMyProfile() {
        Long partnerId = myScope.requireBoundPartnerId();

        EmployeeRespVO employee = employeeMapper.selectByPartnerIdJoin(partnerId);

        CertificatePageReqVO certPage = new CertificatePageReqVO();
        certPage.setPartnerId(partnerId);
        certPage.setPageNo(1);
        certPage.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CertificateRespVO> certificates = certificateService.getCertificatePage(certPage).getList();

        ResumePageReqVO resumePage = new ResumePageReqVO();
        resumePage.setPartnerId(partnerId);
        resumePage.setPageNo(1);
        resumePage.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ResumeRespVO> resumes = resumeService.getResumePage(resumePage).getList();

        HrMyProfileRespVO resp = new HrMyProfileRespVO();
        resp.setEmployee(employee);
        resp.setCertificates(certificates);
        resp.setResumes(resumes);
        return success(resp);
    }

}
