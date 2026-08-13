package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerLevelUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerLevelConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerLevelDO;
import vip.appap.suxin.module.partner.service.PartnerLevelService;
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

@Tag(name = "管理后台 - 会员等级")
@RestController
@RequestMapping("/partner/level")
@Validated
public class PartnerLevelController {

    @Resource
    private PartnerLevelService levelService;

    @PostMapping("/create")
    @Operation(summary = "创建会员等级")
    @PreAuthorize("@ss.hasPermission('partner:level:create')")
    public CommonResult<Long> createLevel(@Valid @RequestBody PartnerLevelCreateReqVO createReqVO) {
        return success(levelService.createLevel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员等级")
    @PreAuthorize("@ss.hasPermission('partner:level:update')")
    public CommonResult<Boolean> updateLevel(@Valid @RequestBody PartnerLevelUpdateReqVO updateReqVO) {
        levelService.updateLevel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员等级")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:level:delete')")
    public CommonResult<Boolean> deleteLevel(@RequestParam("id") Long id) {
        levelService.deleteLevel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员等级")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:level:query')")
    public CommonResult<PartnerLevelRespVO> getLevel(@RequestParam("id") Long id) {
        PartnerLevelDO level = levelService.getLevel(id);
        return success(PartnerLevelConvert.INSTANCE.convert(level));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取会员等级精简信息列表", description = "只包含被开启的会员等级，主要用于前端的下拉选项")
    public CommonResult<List<PartnerLevelRespVO>> getSimpleLevelList() {
        // 获用户列表，只要开启状态的
        List<PartnerLevelDO> list = levelService.getEnableLevelList();
        // 排序后，返回给前端
        return success(PartnerLevelConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员等级分页")
    @PreAuthorize("@ss.hasPermission('partner:level:query')")
    public CommonResult<PageResult<PartnerLevelRespVO>> getLevelPage(@Valid PartnerLevelPageReqVO pageVO) {
        PageResult<PartnerLevelDO> pageResult = levelService.getLevelPage(pageVO);
        return success(PartnerLevelConvert.INSTANCE.convertPage(pageResult));
    }

}
