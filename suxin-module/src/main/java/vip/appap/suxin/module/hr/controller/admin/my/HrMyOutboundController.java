package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundPageReqVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundRespVO;
import vip.appap.suxin.module.hr.controller.admin.outbound.vo.OutboundSaveReqVO;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import vip.appap.suxin.module.hr.service.outbound.OutboundService;
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
 * 职工「外出申请」Controller
 *
 * 路径 /hr/my/outbound，权限 hr:my:outbound:*。仅可操作本人数据。
 *
 * @author suxin
 * @see HrMyContractController 同模式
 */
@Tag(name = "管理后台 - 职工我的外出申请")
@RestController
@RequestMapping("/hr/my/outbound")
@Validated
public class HrMyOutboundController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private OutboundService outboundService;

    @GetMapping("/page")
    @Operation(summary = "获得我的外出分页")
    @PreAuthorize("@ss.hasPermission('hr:my:outbound:query')")
    public CommonResult<PageResult<OutboundRespVO>> getMyOutboundPage(@Valid OutboundPageReqVO pageReqVO) {
        Long partnerId = myScope.requireBoundPartnerId();
        pageReqVO.setPartnerId(partnerId);
        pageReqVO.setName(null);
        return success(outboundService.getOutboundPage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的外出详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:my:outbound:query')")
    public CommonResult<OutboundRespVO> getMyOutbound(@RequestParam("id") Long id) {
        OutboundRespVO outbound = outboundService.getOutbound(id);
        myScope.validateBelongsToMe(outbound.getPartnerId());
        return success(outbound);
    }

    @PostMapping("/create")
    @Operation(summary = "创建我的外出申请")
    @PreAuthorize("@ss.hasPermission('hr:my:outbound:create')")
    public CommonResult<Long> createMyOutbound(@Valid @RequestBody OutboundSaveReqVO createReqVO) {
        createReqVO.setPartnerId(myScope.requireBoundPartnerId());
        return success(outboundService.createOutbound(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的外出申请")
    @PreAuthorize("@ss.hasPermission('hr:my:outbound:update')")
    public CommonResult<Boolean> updateMyOutbound(@Valid @RequestBody OutboundSaveReqVO updateReqVO) {
        OutboundRespVO existing = outboundService.getOutbound(updateReqVO.getId());
        myScope.validateBelongsToMe(existing.getPartnerId());
        updateReqVO.setPartnerId(myScope.requireBoundPartnerId());
        outboundService.updateOutbound(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的外出申请")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:my:outbound:delete')")
    public CommonResult<Boolean> deleteMyOutbound(@RequestParam("id") Long id) {
        OutboundRespVO existing = outboundService.getOutbound(id);
        myScope.validateBelongsToMe(existing.getPartnerId());
        outboundService.deleteOutbound(id);
        return success(true);
    }

}
