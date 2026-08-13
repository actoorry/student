package vip.appap.suxin.module.im.controller.admin;

import cn.hutool.core.collection.CollUtil;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.MapUtils;
import vip.appap.suxin.framework.common.util.number.NumberUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImSensitiveWordPageReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImSensitiveWordRespVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImSensitiveWordSaveReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImSensitiveWordDO;
import vip.appap.suxin.module.im.service.ImSensitiveWordService;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IM 敏感词")
@RestController
@RequestMapping("/im/manager/sensitive-word")
@Validated
public class ImSensitiveWordManagerController {

    @Resource
    private ImSensitiveWordService sensitiveWordService;
    @Resource
    private PartnerApi partnerApi;

    @PostMapping("/create")
    @Operation(summary = "新增敏感词")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:create')")
    public CommonResult<Long> createSensitiveWord(@Valid @RequestBody ImSensitiveWordSaveReqVO reqVO) {
        return success(sensitiveWordService.createSensitiveWord(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改敏感词")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:update')")
    public CommonResult<Boolean> updateSensitiveWord(@Valid @RequestBody ImSensitiveWordSaveReqVO reqVO) {
        sensitiveWordService.updateSensitiveWord(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除敏感词")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:delete')")
    public CommonResult<Boolean> deleteSensitiveWord(@RequestParam("id") Long id) {
        sensitiveWordService.deleteSensitiveWord(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除敏感词")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:delete')")
    public CommonResult<Boolean> deleteSensitiveWordList(@RequestParam("ids") List<Long> ids) {
        sensitiveWordService.deleteSensitiveWordList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得敏感词分页")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:query')")
    public CommonResult<PageResult<ImSensitiveWordRespVO>> getSensitiveWordPage(
            @Valid ImSensitiveWordPageReqVO pageReqVO) {
        // 1. 分页查询
        PageResult<ImSensitiveWordDO> pageResult = sensitiveWordService.getSensitiveWordPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 2.1 批量查询创建人昵称
        Map<Long, PartnerRespDTO> userMap = partnerApi.getUserMap(convertSet(pageResult.getList(),
                word -> NumberUtils.parseLong(word.getCreator())));
        // 2.2 转换为 VO，填充创建人昵称
        return success(BeanUtils.toBean(pageResult, ImSensitiveWordRespVO.class, vo ->
                MapUtils.findAndThen(userMap, NumberUtils.parseLong(vo.getCreator()),
                        user -> vo.setCreatorName(user.getNickname()))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得敏感词详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:sensitive-word:query')")
    public CommonResult<ImSensitiveWordRespVO> getSensitiveWord(@RequestParam("id") Long id) {
        ImSensitiveWordDO word = sensitiveWordService.getSensitiveWord(id);
        return success(BeanUtils.toBean(word, ImSensitiveWordRespVO.class));
    }

}
