package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificatePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateRespVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateSaveReqVO;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import vip.appap.suxin.module.hr.service.certificate.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * 职工「我的证书」Controller
 *
 * 路径 /hr/my/certificate，权限 hr:my:certificate:*。仅可操作本人数据。
 * 不暴露督查台账 /ledger 系列接口。
 *
 * @author suxin
 * @see HrMyContractController 同模式
 */
@Tag(name = "管理后台 - 职工我的证书")
@RestController
@RequestMapping("/hr/my/certificate")
@Validated
public class HrMyCertificateController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private CertificateService certificateService;

    @GetMapping("/page")
    @Operation(summary = "获得我的证书分页")
    @PreAuthorize("@ss.hasPermission('hr:my:certificate:query')")
    public CommonResult<PageResult<CertificateRespVO>> getMyCertificatePage(@Valid CertificatePageReqVO pageReqVO) {
        Long partnerId = myScope.requireBoundPartnerId();
        pageReqVO.setPartnerId(partnerId);
        pageReqVO.setName(null);
        // 证书 status 计算依赖字段，无需禁用；保留本人按状态筛选
        return success(certificateService.getCertificatePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的证书详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:my:certificate:query')")
    public CommonResult<CertificateRespVO> getMyCertificate(@RequestParam("id") Long id) {
        CertificateRespVO certificate = certificateService.getCertificate(id);
        myScope.validateBelongsToMe(certificate.getPartnerId());
        return success(certificate);
    }

    @PostMapping("/create")
    @Operation(summary = "创建我的证书")
    @PreAuthorize("@ss.hasPermission('hr:my:certificate:create')")
    public CommonResult<Long> createMyCertificate(@Valid @RequestBody CertificateSaveReqVO createReqVO) {
        createReqVO.setPartnerId(myScope.requireBoundPartnerId());
        return success(certificateService.createCertificate(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的证书")
    @PreAuthorize("@ss.hasPermission('hr:my:certificate:update')")
    public CommonResult<Boolean> updateMyCertificate(@Valid @RequestBody CertificateSaveReqVO updateReqVO) {
        CertificateRespVO existing = certificateService.getCertificate(updateReqVO.getId());
        myScope.validateBelongsToMe(existing.getPartnerId());
        updateReqVO.setPartnerId(myScope.requireBoundPartnerId());
        certificateService.updateCertificate(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的证书")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:my:certificate:delete')")
    public CommonResult<Boolean> deleteMyCertificate(@RequestParam("id") Long id) {
        CertificateRespVO existing = certificateService.getCertificate(id);
        myScope.validateBelongsToMe(existing.getPartnerId());
        certificateService.deleteCertificate(id);
        return success(true);
    }

}
