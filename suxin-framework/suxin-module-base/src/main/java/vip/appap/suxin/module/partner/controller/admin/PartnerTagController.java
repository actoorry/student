package vip.appap.suxin.module.partner.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagCreateReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagPageReqVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagRespVO;
import vip.appap.suxin.module.partner.controller.admin.vo.PartnerTagUpdateReqVO;
import vip.appap.suxin.module.partner.convert.PartnerTagConvert;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerTagDO;
import vip.appap.suxin.module.partner.service.PartnerTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 会员标签")
@RestController
@RequestMapping("/partner/tag")
@Validated
public class PartnerTagController {

    @Resource
    private PartnerTagService tagService;

    @PostMapping("/create")
    @Operation(summary = "创建会员标签")
    @PreAuthorize("@ss.hasPermission('partner:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody PartnerTagCreateReqVO createReqVO) {
        return success(tagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员标签")
    @PreAuthorize("@ss.hasPermission('partner:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody PartnerTagUpdateReqVO updateReqVO) {
        tagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('partner:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员标签")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('partner:tag:query')")
    public CommonResult<PartnerTagRespVO> getPartnerTag(@RequestParam("id") Long id) {
        PartnerTagDO tag = tagService.getTag(id);
        return success(PartnerTagConvert.INSTANCE.convert(tag));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获取会员标签精简信息列表", description = "只包含被开启的会员标签，主要用于前端的下拉选项")
    public CommonResult<List<PartnerTagRespVO>> getSimpleTagList() {
        // 获用户列表，只要开启状态的
        List<PartnerTagDO> list = tagService.getTagList();
        // 排序后，返回给前端
        return success(PartnerTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list")
    @Operation(summary = "获得会员标签列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('partner:tag:query')")
    public CommonResult<List<PartnerTagRespVO>> getPartnerTagList(@RequestParam("ids") Collection<Long> ids) {
        List<PartnerTagDO> list = tagService.getTagList(ids);
        return success(PartnerTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员标签分页")
    @PreAuthorize("@ss.hasPermission('partner:tag:query')")
    public CommonResult<PageResult<PartnerTagRespVO>> getTagPage(@Valid PartnerTagPageReqVO pageVO) {
        PageResult<PartnerTagDO> pageResult = tagService.getTagPage(pageVO);
        return success(PartnerTagConvert.INSTANCE.convertPage(pageResult));
    }

}
