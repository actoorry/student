package vip.appap.suxin.module.rongjh.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorAuditReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorPageReqVO;
import vip.appap.suxin.module.rongjh.controller.admin.vo.WarriorRespVO;
import vip.appap.suxin.module.rongjh.service.WarriorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 戎集汇战友会")
@RestController
@RequestMapping("/rongjh/warrior")
@Validated
public class WarriorAdminController {

    @Resource
    private WarriorService warriorService;

    @GetMapping("/page")
    @Operation(summary = "获得战友会身份分页")
    @PreAuthorize("@ss.hasPermission('rongjh:warrior:query')")
    public CommonResult<PageResult<WarriorRespVO>> getWarriorPage(@Valid WarriorPageReqVO pageVO) {
        return success(warriorService.getWarriorPage(pageVO));
    }

    @GetMapping("/get")
    @Operation(summary = "获得战友会身份详情")
    @Parameter(name = "id", description = "会员编号(partner.id)", required = true)
    @PreAuthorize("@ss.hasPermission('rongjh:warrior:query')")
    public CommonResult<WarriorRespVO> getWarrior(@RequestParam("id") Long id) {
        return success(warriorService.getWarriorDetail(id));
    }

    @PutMapping("/approve")
    @Operation(summary = "审核通过")
    @PreAuthorize("@ss.hasPermission('rongjh:warrior:audit')")
    public CommonResult<Boolean> approve(@Valid @RequestBody WarriorAuditReqVO reqVO) {
        warriorService.approveWarrior(reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "审核驳回")
    @PreAuthorize("@ss.hasPermission('rongjh:warrior:audit')")
    public CommonResult<Boolean> reject(@Valid @RequestBody WarriorAuditReqVO reqVO) {
        warriorService.rejectWarrior(reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PutMapping("/blacklist")
    @Operation(summary = "拉黑成员")
    @PreAuthorize("@ss.hasPermission('rongjh:warrior:audit')")
    public CommonResult<Boolean> blacklist(@Valid @RequestBody WarriorAuditReqVO reqVO) {
        warriorService.blacklistWarrior(reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

}
