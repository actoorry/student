package vip.appap.suxin.module.hr.controller.admin.appointment;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentNotifyLogRespVO;
import vip.appap.suxin.module.hr.service.appointment.AppointmentNotifyLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HR 聘期推送日志")
@RestController
@RequestMapping("/hr/appointment/notify-log")
@Validated
public class AppointmentNotifyLogController {

    @Resource
    private AppointmentNotifyLogService notifyLogService;

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('hr:appointment:notifylog:query')")
    public CommonResult<PageResult<AppointmentNotifyLogRespVO>> getNotifyLogPage(
            @Valid AppointmentNotifyLogPageReqVO pageReqVO) {
        return success(notifyLogService.getNotifyLogPage(pageReqVO));
    }

}
