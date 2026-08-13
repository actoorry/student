package vip.appap.suxin.module.hr.controller.admin.outbound;

import vip.appap.suxin.framework.apilog.core.annotation.ApiAccessLog;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.excel.core.util.ExcelUtils;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundRespVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundSaveReqVO;
import vip.appap.suxin.module.hr.service.outbound.OutboundService;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

/**
 * HR 人员外出管理 Controller
 *
 * 路径 /hr/outbound，权限 hr:outbound:*。不走审批流程，人事自行填写维护。
 *
 * @author suxin
 */
@Tag(name = "管理后台 - HR 人员外出管理")
@RestController
@RequestMapping("/hr/outbound")
@Validated
public class OutboundController {

    @Resource
    private OutboundService outboundService;

    @PostMapping("/create")
    @Operation(summary = "新增外出记录")
    @PreAuthorize("@ss.hasPermission('hr:outbound:create')")
    public CommonResult<Long> createOutbound(@Valid @RequestBody OutboundSaveReqVO createReqVO) {
        return success(outboundService.createOutbound(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新外出记录")
    @PreAuthorize("@ss.hasPermission('hr:outbound:update')")
    public CommonResult<Boolean> updateOutbound(@Valid @RequestBody OutboundSaveReqVO updateReqVO) {
        outboundService.updateOutbound(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得外出记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:outbound:query')")
    public CommonResult<OutboundRespVO> getOutbound(@RequestParam("id") Long id) {
        return success(outboundService.getOutbound(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得外出记录分页")
    @PreAuthorize("@ss.hasPermission('hr:outbound:query')")
    public CommonResult<PageResult<OutboundRespVO>> getOutboundPage(@Valid OutboundPageReqVO pageReqVO) {
        return success(outboundService.getOutboundPage(pageReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除外出记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:outbound:delete')")
    public CommonResult<Boolean> deleteOutbound(@RequestParam("id") Long id) {
        outboundService.deleteOutbound(id);
        return success(true);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出外出记录 Excel")
    @PreAuthorize("@ss.hasPermission('hr:outbound:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOutboundExcel(@Valid OutboundPageReqVO pageReqVO,
                                    HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<OutboundRespVO> list = outboundService.getOutboundPage(pageReqVO).getList();
        ExcelUtils.write(response, "外出记录.xls", "数据", OutboundRespVO.class, list);
    }

    // ========== 员工档案汇总（职称评审视角） ==========

    @GetMapping("/summary")
    @Operation(summary = "获得员工外出汇总（累计服务年限 + 累计继教学分）")
    @Parameter(name = "partnerId", description = "员工 partner.id", required = true)
    @PreAuthorize("@ss.hasPermission('hr:outbound:query')")
    public CommonResult<Map<String, Object>> getOutboundSummary(@RequestParam("partnerId") Long partnerId) {
        BigDecimal supportYears = outboundService.sumSupportYears(partnerId);
        BigDecimal credit = outboundService.sumContinuingEducationCredit(partnerId);
        return success(Map.of(
                "supportYears", supportYears,
                "continuingEducationCredit", credit
        ));
    }

}
