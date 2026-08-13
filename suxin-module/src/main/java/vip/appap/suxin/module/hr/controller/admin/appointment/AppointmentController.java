package vip.appap.suxin.module.hr.controller.admin.appointment;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentBatchSaveReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentRespVO;
import vip.appap.suxin.module.hr.controller.admin.appointment.vo.AppointmentSaveReqVO;
import vip.appap.suxin.module.hr.service.appointment.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - HR 岗位聘任")
@RestController
@RequestMapping("/hr/appointment")
@Validated
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @PostMapping("/create")
    @Operation(summary = "新增岗位聘任")
    @PreAuthorize("@ss.hasPermission('hr:appointment:create')")
    public CommonResult<Long> createAppointment(@Valid @RequestBody AppointmentSaveReqVO createReqVO) {
        return success(appointmentService.createAppointment(createReqVO));
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量岗位聘任")
    @PreAuthorize("@ss.hasPermission('hr:appointment:batch')")
    public CommonResult<Integer> batchCreateAppointment(@Valid @RequestBody AppointmentBatchSaveReqVO batchReqVO) {
        return success(appointmentService.batchCreateAppointment(batchReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新岗位聘任")
    @PreAuthorize("@ss.hasPermission('hr:appointment:update')")
    public CommonResult<Boolean> updateAppointment(@Valid @RequestBody AppointmentSaveReqVO updateReqVO) {
        appointmentService.updateAppointment(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除岗位聘任")
    @PreAuthorize("@ss.hasPermission('hr:appointment:delete')")
    public CommonResult<Boolean> deleteAppointment(@RequestParam("id") Long id) {
        appointmentService.deleteAppointment(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得岗位聘任")
    @PreAuthorize("@ss.hasPermission('hr:appointment:query')")
    public CommonResult<AppointmentRespVO> getAppointment(@RequestParam("id") Long id) {
        return success(appointmentService.getAppointment(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得岗位聘任分页")
    @PreAuthorize("@ss.hasPermission('hr:appointment:query')")
    public CommonResult<PageResult<AppointmentRespVO>> getAppointmentPage(@Valid AppointmentPageReqVO pageReqVO) {
        return success(appointmentService.getAppointmentPage(pageReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出岗位聘任 Excel")
    @PreAuthorize("@ss.hasPermission('hr:appointment:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAppointmentExcel(@Valid AppointmentPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<AppointmentRespVO> list = appointmentService.getAppointmentPage(pageReqVO).getList();
        ExcelUtils.write(response, "岗位聘任.xls", "数据", AppointmentRespVO.class, list);
    }

    @GetMapping("/current-list")
    @Operation(summary = "获得员工当前聘任记录")
    @PreAuthorize("@ss.hasPermission('hr:appointment:query')")
    public CommonResult<List<AppointmentRespVO>> getCurrentAppointmentList(
            @RequestParam("partnerId") Long partnerId) {
        return success(appointmentService.getCurrentAppointmentList(partnerId));
    }

    @GetMapping("/expiring-summary")
    @Operation(summary = "聘期即将到期汇总（90天内）")
    @PreAuthorize("@ss.hasPermission('hr:appointment:query')")
    public CommonResult<Map<String, Object>> getExpiringSummary() {
        AppointmentPageReqVO req = new AppointmentPageReqVO();
        req.setPageSize(PageParam.PAGE_SIZE_NONE);
        req.setStatus("active");
        req.setIsCurrent(1);
        List<AppointmentRespVO> all = appointmentService.getAppointmentPage(req).getList();
        long expiring90 = all.stream()
                .filter(a -> a.getDaysToExpire() != null && a.getDaysToExpire() >= 0 && a.getDaysToExpire() <= 90)
                .count();
        return success(Map.of("expiring90", expiring90, "totalActive", all.size()));
    }

}
