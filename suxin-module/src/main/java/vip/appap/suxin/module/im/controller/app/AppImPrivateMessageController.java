package vip.appap.suxin.module.im.controller.app;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.CommonResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.app.vo.AppImPrivateMessageRespVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageSendReqVO;
import vip.appap.suxin.module.im.controller.app.vo.AppImPrivateMessageSendReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImPrivateMessageDO;
import vip.appap.suxin.module.im.service.ImPrivateMessageService;
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
 * App 端 - IM 私聊消息 Controller
 */
@Tag(name = "App 端 - IM 私聊消息")
@RestController
@RequestMapping("/im/message/private")
@Validated
public class AppImPrivateMessageController {

    @Resource
    private ImPrivateMessageService privateMessageService;

    @PostMapping("/send")
    @Operation(summary = "发送私聊消息")
    public CommonResult<AppImPrivateMessageRespVO> sendPrivateMessage(
            @Valid @RequestBody AppImPrivateMessageSendReqVO reqVO) {
        // App 端使用 MEMBER 用户类型；转换 App VO → Service 层 VO
        ImPrivateMessageSendReqVO serviceReqVO = BeanUtils.toBean(reqVO, ImPrivateMessageSendReqVO.class);
        ImPrivateMessageDO message = privateMessageService.sendPrivateMessage(
                getLoginUserId(), serviceReqVO, UserTypeEnum.MEMBER.getValue());
        return success(BeanUtils.toBean(message, AppImPrivateMessageRespVO.class));
    }

    @GetMapping("/pull")
    @Operation(summary = "拉取私聊消息（增量）")
    @Parameter(name = "minId", description = "最小消息 id", required = true, example = "0")
    @Parameter(name = "size", description = "拉取数量", required = true, example = "100")
    public CommonResult<List<AppImPrivateMessageRespVO>> pullPrivateMessageList(
            @RequestParam("minId") Long minId,
            @RequestParam("size") Integer size) {
        List<ImPrivateMessageDO> messages = privateMessageService.pullPrivateMessageList(getLoginUserId(), minId, size);
        return success(BeanUtils.toBean(messages, AppImPrivateMessageRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "查询私聊历史消息（游标拉取）")
    @Parameter(name = "receiverId", description = "接收人编号", required = true, example = "2")
    @Parameter(name = "maxId", description = "最大消息编号（不含），用于向上翻页", example = "0")
    @Parameter(name = "limit", description = "拉取数量", example = "20")
    public CommonResult<List<AppImPrivateMessageRespVO>> getPrivateMessageList(
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "maxId", required = false) Long maxId,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        // 复用现有的 ImPrivateMessageListReqVO
        vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageListReqVO reqVO =
                new vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageListReqVO();
        reqVO.setReceiverId(receiverId);
        reqVO.setMaxId(maxId);
        reqVO.setLimit(limit);
        List<ImPrivateMessageDO> messages = privateMessageService.getPrivateMessageList(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(messages, AppImPrivateMessageRespVO.class));
    }

    @PutMapping("/read")
    @Operation(summary = "标记私聊消息已读")
    @Parameter(name = "receiverId", description = "接收方用户编号（对方）", required = true, example = "2")
    @Parameter(name = "messageId", description = "已读位置（含），通常是会话内最大消息编号", required = true, example = "100")
    public CommonResult<Boolean> readPrivateMessages(
            @RequestParam("receiverId") Long receiverId,
            @RequestParam("messageId") Long messageId) {
        privateMessageService.readPrivateMessages(getLoginUserId(), receiverId, messageId);
        return success(true);
    }

    @GetMapping("/max-read-message-id")
    @Operation(summary = "查询对方已读到我发的最大消息 id")
    @Parameter(name = "peerId", description = "对方用户编号", required = true, example = "2")
    public CommonResult<Long> getMaxReadMessageId(@RequestParam("peerId") Long peerId) {
        return success(privateMessageService.getMaxReadMessageId(getLoginUserId(), peerId));
    }

    @DeleteMapping("/recall")
    @Operation(summary = "撤回私聊消息")
    @Parameter(name = "id", description = "消息编号", required = true, example = "1")
    public CommonResult<AppImPrivateMessageRespVO> recallPrivateMessage(@RequestParam("id") Long id) {
        ImPrivateMessageDO message = privateMessageService.recallPrivateMessage(getLoginUserId(), id);
        return success(BeanUtils.toBean(message, AppImPrivateMessageRespVO.class));
    }

}
