package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerGroupUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerGroupConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerGroupDO;
import vip.appap.suxin.module.partner.service.PartnerGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 用户分组")
@RestController
@RequestMapping("/partner/group")
@Validated
public class PartnerGroupController {

    @Resource
    private PartnerGroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建用户分组")
    @PreAuthorize("@ss.hasPermission('partner:group:create')")
    public CommonResult<Long> createGroup(@Valid @RequestBody PartnerGroupCreateReqVO createReqVO) {
        return success(groupService.createGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户分组")
    @PreAuthorize("@ss.hasPermission('partner:group:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody PartnerGroupUpdateReqVO updateReqVO) {
        groupService.updateGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户分组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        groupService.deleteGroup(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户分组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:group:query')")
    public CommonResult<PartnerGroupRespVO> getGroup(@RequestParam("id") Long id) {
        PartnerGroupDO group = groupService.getGroup(id);
        return success(PartnerGroupConvert.INSTANCE.convert(group));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取用户分组精简信息列表", description = "只包含被开启的用户分组，主要用于前端的下拉选项")
    public CommonResult<List<PartnerGroupRespVO>> getSimpleGroupList() {
        // 获用户列表，只要开启状态的
        List<PartnerGroupDO> list = groupService.getEnableGroupList();
        // 排序后，返回给前端
        return success(PartnerGroupConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户分组分页")
    @PreAuthorize("@ss.hasPermission('partner:group:query')")
    public CommonResult<PageResult<PartnerGroupRespVO>> getGroupPage(@Valid PartnerGroupPageReqVO pageVO) {
        PageResult<PartnerGroupDO> pageResult = groupService.getGroupPage(pageVO);
        return success(PartnerGroupConvert.INSTANCE.convertPage(pageResult));
    }

}
