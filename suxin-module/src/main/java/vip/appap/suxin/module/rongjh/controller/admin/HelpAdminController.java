package vip.appap.suxin.module.rongjh.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpAuditReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpPageReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.HelpRespVO;
import vip.appap.suxin.module.rongjh.service.HelpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 戎集汇戎爱心")
@RestController
@RequestMapping("/rongjh/help")
@Validated
public class HelpAdminController {

    @Resource
    private HelpService helpService;

    @GetMapping("/page")
    @Operation(summary = "获得爱心帮扶申请分页")
    @PreAuthorize("@ss.hasPermission('rongjh:help:query')")
    public CommonResult<PageResult<HelpRespVO>> getHelpPage(@Valid HelpPageReqVO pageVO) {
        return success(helpService.getHelpPage(pageVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得爱心帮扶申请详情")
    @Parameter(name = "id", description = "申请编号", required = true)
    @PreAuthorize("@ss.hasPermission('rongjh:help:query')")
    public CommonResult<HelpRespVO> getHelp(@RequestParam("id") Long id) {
        return success(helpService.getHelpDetail(id));
    }

    @PutMapping("/approve")
    @Operation(summary = "审核通过")
    @PreAuthorize("@ss.hasPermission('rongjh:help:audit')")
    public CommonResult<Boolean> approve(@Valid @RequestBody HelpAuditReqVO reqVO) {
        helpService.approveHelp(reqVO.getId(), reqVO.getActualAmount(), reqVO.getRemark());
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "审核驳回")
    @PreAuthorize("@ss.hasPermission('rongjh:help:audit')")
    public CommonResult<Boolean> reject(@Valid @RequestBody HelpAuditReqVO reqVO) {
        helpService.rejectHelp(reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

}
