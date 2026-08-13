package vip.appap.suxin.module.hr.controller.admin.certificate;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.certificate.vo.CertificateNotifyLogRespVO;
import vip.appap.suxin.module.hr.service.certificate.CertificateNotifyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 证书推送日志")
@RestController
@RequestMapping("/hr/certificate/notify-log")
@Validated
public class CertificateNotifyLogController {

    @Resource
    private CertificateNotifyLogService notifyLogService;

    @GetMapping("/page")
    @Operation(summary = "获得证书推送日志分页")
    @PreAuthorize("@ss.hasPermission('hr:certificate:notifylog:query')")
    public CommonResult<PageResult<CertificateNotifyLogRespVO>> getNotifyLogPage(@Valid CertificateNotifyLogPageReqVO pageReqVO) {
        return success(notifyLogService.getNotifyLogPage(pageReqVO));
    }

    @PostMapping("/retry")
    @Operation(summary = "补推一条失败的推送日志")
    @Parameter(name = "id", description = "推送日志编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:certificate:notifylog:retry')")
    public CommonResult<Boolean> retryNotifyLog(@RequestParam("id") Long id) {
        notifyLogService.retryNotifyLog(id);
        return success(true);
    }

}
