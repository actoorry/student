package vip.appap.suxin.module.sales.controller.admin;

import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuConversationRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuConversationUpdatePinnedReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuConversationDO;
import vip.appap.suxin.module.sales.service.SalesKeFuConversationService;
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
import static vip.appap.suxin.framework.common.util.collection.MapUtils.findAndThen;

@Tag(name = "管理后台 - 客服会话")
@RestController
@RequestMapping("/sales/promotion/kefu-conversation")
@Validated
public class SalesKeFuConversationController {

    @Resource
    private SalesKeFuConversationService conversationService;
    @Resource
    private PartnerApi PartnerApi;

    @GetMapping("/get")
    @Operation(summary = "获得客服会话")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_conversation:query')")
    public CommonResult<SalesKeFuConversationRespVO> getConversation(@RequestParam("id") Long id) {
        SalesKeFuConversationDO conversation = conversationService.getConversation(id);
        if (conversation == null) {
            return success(null);
        }

        // 拼接数据
        SalesKeFuConversationRespVO result = BeanUtils.toBean(conversation, SalesKeFuConversationRespVO.class);
        PartnerRespDTO PartnerUser = PartnerApi.getUser(conversation.getUserId());
        if (PartnerUser != null) {
            result.setUserAvatar(PartnerUser.getAvatar()).setUserNickname(PartnerUser.getNickname());
        }
        return success(result);
    }

    @PutMapping("/update-conversation-pinned")
    @Operation(summary = "置顶/取消置顶客服会话")
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_conversation:update')")
    public CommonResult<Boolean> updateConversationPinned(@Valid @RequestBody SalesKeFuConversationUpdatePinnedReqVO updateReqVO) {
        conversationService.updateConversationPinnedByAdmin(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客服会话")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_conversation:delete')")
    public CommonResult<Boolean> deleteConversation(@RequestParam("id") Long id) {
        conversationService.deleteKefuConversation(id);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得客服会话列表")
    @PreAuthorize("@ss.hasPermission('sales:sales_kefu_conversation:query')")
    public CommonResult<List<SalesKeFuConversationRespVO>> getConversationList() {
        // 查询会话列表
        List<SalesKeFuConversationRespVO> respList = BeanUtils.toBean(conversationService.getKefuConversationList(),
                SalesKeFuConversationRespVO.class);

        // 拼接数据
        Map<Long, PartnerRespDTO> userMap = PartnerApi.getUserMap(convertSet(respList, SalesKeFuConversationRespVO::getUserId));
        respList.forEach(item-> findAndThen(userMap, item.getUserId(),
                PartnerUser-> item.setUserAvatar(PartnerUser.getAvatar()).setUserNickname(PartnerUser.getNickname())));
        return success(respList);
    }

}