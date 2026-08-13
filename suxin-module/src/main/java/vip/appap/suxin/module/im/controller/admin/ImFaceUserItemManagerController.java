package vip.appap.suxin.module.im.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.collection.CollectionUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImFaceUserItemManagerPageReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImFaceUserItemManagerRespVO;
import vip.appap.suxin.module.im.dal.dataobject.ImFaceUserItemDO;
import vip.appap.suxin.module.im.service.ImFaceUserItemService;
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

@Tag(name = "管理后台 - IM 用户表情")
@RestController
@RequestMapping("/im/manager/face-user-item")
@Validated
public class ImFaceUserItemManagerController {

    @Resource
    private ImFaceUserItemService faceUserItemService;
    @Resource
    private PartnerApi partnerApi;

    @GetMapping("/page")
    @Operation(summary = "获得用户表情分页")
    @PreAuthorize("@ss.hasPermission('im:manager:face-user-item:query')")
    public CommonResult<PageResult<ImFaceUserItemManagerRespVO>> getFaceUserItemPage(
            @Valid ImFaceUserItemManagerPageReqVO pageReqVO) {
        PageResult<ImFaceUserItemDO> pageResult = faceUserItemService.getFaceUserItemPage(pageReqVO);
        // 关联回填用户昵称
        Map<Long, PartnerRespDTO> userMap = partnerApi.getUserMap(
                CollectionUtils.convertSet(pageResult.getList(), ImFaceUserItemDO::getUserId));
        List<ImFaceUserItemManagerRespVO> voList = CollectionUtils.convertList(pageResult.getList(), item -> {
            ImFaceUserItemManagerRespVO vo = BeanUtils.toBean(item, ImFaceUserItemManagerRespVO.class);
            PartnerRespDTO user = userMap.get(item.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }
            return vo;
        });
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户表情")
    @Parameter(name = "id", description = "编号", required = true, example = "4096")
    @PreAuthorize("@ss.hasPermission('im:manager:face-user-item:delete')")
    public CommonResult<Boolean> deleteFaceUserItem(@RequestParam("id") Long id) {
        faceUserItemService.deleteFaceUserItem(id);
        return success(true);
    }

}
