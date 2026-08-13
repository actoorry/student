package vip.appap.suxin.module.marriage.controller.app;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.app.vo.AppImPrivateMessageRespVO;
import vip.appap.suxin.module.im.controller.app.vo.AppImPrivateMessageSendReqVO;
import vip.appap.suxin.module.im.controller.app.vo.AppImConversationRespVO;
import vip.appap.suxin.module.im.dal.dataobject.ImPrivateMessageDO;
import vip.appap.suxin.module.im.service.ImPrivateMessageServiceImpl;
import vip.appap.suxin.module.im.service.dto.ImPrivateMessageSendDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static vip.appap.suxin.framework.common.pojo.CommonResult.success;
import static vip.appap.suxin.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端 - 婚恋私聊 Controller
 *
 * 基于 IM 模块的内部接口实现，绕过好友校验，由 marriage 模块自行控制聊天权限
 */
@Tag(name = "App 端 - 婚恋私聊")
@RestController
@RequestMapping("/marriage/chat")
@Validated
public class AppMarriageChatController {

    @Resource
    private ImPrivateMessageServiceImpl imPrivateMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送私聊消息")
    public CommonResult<AppImPrivateMessageRespVO> sendMessage(
            @Valid @RequestBody AppImPrivateMessageSendReqVO reqVO) {
        // 构建内部 DTO，调用 IM 的无好友校验接口，指定 MEMBER 类型
        ImPrivateMessageSendDTO dto = new ImPrivateMessageSendDTO()
                .setReceiverId(reqVO.getReceiverId())
                .setType(reqVO.getType())
                .setContent(reqVO.getContent());
        ImPrivateMessageDO message = imPrivateMessageService.sendPrivateMessage(
                getLoginUserId(), dto, UserTypeEnum.MEMBER.getValue());
        return success(BeanUtils.toBean(message, AppImPrivateMessageRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "查询私聊历史消息（游标拉取）")
    @Parameter(name = "peerId", description = "对方用户编号", required = true, example = "2")
    @Parameter(name = "maxId", description = "最大消息编号（不含），用于向上翻页", example = "0")
    @Parameter(name = "limit", description = "拉取数量", example = "20")
    public CommonResult<List<AppImPrivateMessageRespVO>> getChatHistory(
            @RequestParam("peerId") Long peerId,
            @RequestParam(value = "maxId", required = false) Long maxId,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        // 复用 IM 的历史消息查询接口
        vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageListReqVO reqVO =
                new vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageListReqVO();
        reqVO.setReceiverId(peerId);
        reqVO.setMaxId(maxId);
        reqVO.setLimit(limit);
        List<ImPrivateMessageDO> messages = imPrivateMessageService.getPrivateMessageList(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(messages, AppImPrivateMessageRespVO.class));
    }

    @PutMapping("/read")
    @Operation(summary = "标记私聊消息已读")
    @Parameter(name = "peerId", description = "对方用户编号", required = true, example = "2")
    @Parameter(name = "messageId", description = "已读位置（含），通常是会话内最大消息编号", required = true, example = "100")
    public CommonResult<Boolean> markAsRead(
            @RequestParam("peerId") Long peerId,
            @RequestParam("messageId") Long messageId) {
        imPrivateMessageService.readPrivateMessages(getLoginUserId(), peerId, messageId);
        return success(true);
    }

    @GetMapping("/max-read-message-id")
    @Operation(summary = "查询对方已读到我发的最大消息 id")
    @Parameter(name = "peerId", description = "对方用户编号", required = true, example = "2")
    public CommonResult<Long> getMaxReadMessageId(@RequestParam("peerId") Long peerId) {
        return success(imPrivateMessageService.getMaxReadMessageId(getLoginUserId(), peerId));
    }

    @DeleteMapping("/recall")
    @Operation(summary = "撤回私聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<AppImPrivateMessageRespVO> recallMessage(@RequestParam("id") Long id) {
        ImPrivateMessageDO message = imPrivateMessageService.recallPrivateMessage(getLoginUserId(), id);
        return success(BeanUtils.toBean(message, AppImPrivateMessageRespVO.class));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取私信未读数量")
    public CommonResult<Long> getUnreadCount() {
        return success(imPrivateMessageService.getUnreadCount(getLoginUserId()));
    }

    @GetMapping("/conversation-list")
    @Operation(summary = "获取私信会话列表")
    public CommonResult<List<AppImConversationRespVO>> getConversationList() {
        return success(BeanUtils.toBean(imPrivateMessageService.getConversationList(getLoginUserId()),
                AppImConversationRespVO.class));
    }

}
