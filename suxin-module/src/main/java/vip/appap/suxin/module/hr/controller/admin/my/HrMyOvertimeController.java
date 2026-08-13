package vip.appap.suxin.module.hr.controller.admin.my;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.OvertimePageReqVO;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.OvertimeRespVO;
import vip.appap.suxin.module.hr.controller.admin.overtime.vo.OvertimeSaveReqVO;
import vip.appap.suxin.module.hr.framework.my.HrMyScopeSupport;
import vip.appap.suxin.module.hr.service.overtime.OvertimeService;
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
 * 职工「加班登记」Controller
 *
 * 路径 /hr/my/overtime，权限 hr:my:overtime:*。仅可操作本人数据。
 *
 * @author suxin
 * @see HrMyContractController 同模式
 */
@Tag(name = "管理后台 - 职工我的加班登记")
@RestController
@RequestMapping("/hr/my/overtime")
@Validated
public class HrMyOvertimeController {

    @Resource
    private HrMyScopeSupport myScope;
    @Resource
    private OvertimeService overtimeService;

    @GetMapping("/page")
    @Operation(summary = "获得我的加班分页")
    @PreAuthorize("@ss.hasPermission('hr:my:overtime:query')")
    public CommonResult<PageResult<OvertimeRespVO>> getMyOvertimePage(@Valid OvertimePageReqVO pageReqVO) {
        Long partnerId = myScope.requireBoundPartnerId();
        pageReqVO.setPartnerId(partnerId);
        pageReqVO.setName(null);
        return success(overtimeService.getOvertimePage(pageReqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得我的加班详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('hr:my:overtime:query')")
    public CommonResult<OvertimeRespVO> getMyOvertime(@RequestParam("id") Long id) {
        OvertimeRespVO overtime = overtimeService.getOvertime(id);
        myScope.validateBelongsToMe(overtime.getPartnerId());
        return success(overtime);
    }

    @PostMapping("/create")
    @Operation(summary = "创建我的加班登记")
    @PreAuthorize("@ss.hasPermission('hr:my:overtime:create')")
    public CommonResult<Long> createMyOvertime(@Valid @RequestBody OvertimeSaveReqVO createReqVO) {
        createReqVO.setPartnerId(myScope.requireBoundPartnerId());
        return success(overtimeService.createOvertime(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新我的加班登记")
    @PreAuthorize("@ss.hasPermission('hr:my:overtime:update')")
    public CommonResult<Boolean> updateMyOvertime(@Valid @RequestBody OvertimeSaveReqVO updateReqVO) {
        OvertimeRespVO existing = overtimeService.getOvertime(updateReqVO.getId());
        myScope.validateBelongsToMe(existing.getPartnerId());
        updateReqVO.setPartnerId(myScope.requireBoundPartnerId());
        overtimeService.updateOvertime(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除我的加班登记")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hr:my:overtime:delete')")
    public CommonResult<Boolean> deleteMyOvertime(@RequestParam("id") Long id) {
        OvertimeRespVO existing = overtimeService.getOvertime(id);
        myScope.validateBelongsToMe(existing.getPartnerId());
        overtimeService.deleteOvertime(id);
        return success(true);
    }

}
