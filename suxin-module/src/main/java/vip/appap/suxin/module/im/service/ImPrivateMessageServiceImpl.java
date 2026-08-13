package vip.appap.suxin.module.im.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.json.JsonUtils;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageManagerPageReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageListReqVO;
import vip.appap.suxin.module.im.controller.admin.vo.ImPrivateMessageSendReqVO;
import vip.appap.suxin.module.im.dal.dataobject.ImPrivateMessageDO;
import vip.appap.suxin.module.im.dal.mysql.ImPrivateMessageMapper;
import vip.appap.suxin.module.im.enums.ImMessageStatusEnum;
import vip.appap.suxin.module.im.enums.ImMessageTypeEnum;
import vip.appap.suxin.module.im.framework.config.ImProperties;
import vip.appap.suxin.module.im.service.ImFriendService;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.im.service.dto.ImPrivateMessageSendDTO;
import vip.appap.suxin.module.im.service.ImConversationBO;
import vip.appap.suxin.module.im.service.ImConversationRespDTO;
import vip.appap.suxin.module.im.service.ImSensitiveWordService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vip.appap.suxin.module.im.service.websocket.ImWebSocketService;
import vip.appap.suxin.module.im.service.websocket.dto.ImPrivateMessageDTO;
import vip.appap.suxin.module.im.service.websocket.dto.message.QuoteMessage;
import vip.appap.suxin.module.im.service.websocket.dto.message.RecallMessage;
import vip.appap.suxin.module.im.util.ImMessageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.im.enums.ErrorCodeConstants.*;

/**
 * IM 私聊消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class ImPrivateMessageServiceImpl implements ImPrivateMessageService {

    @Resource
    private ImPrivateMessageMapper privateMessageMapper;

    @Resource
    private ImFriendService friendService;
    @Resource
    private ImSensitiveWordService sensitiveWordService;

    @Resource
    private ImWebSocketService imWebSocketService;

    @Resource
    private ImProperties imProperties;

    @Resource
    private PartnerApi partnerApi;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendReqVO reqVO) {
        // 默认 ADMIN 用户类型，兼容现有管理后台调用
        return sendPrivateMessage(senderId, reqVO, UserTypeEnum.ADMIN.getValue());
    }

    @Override
    public ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendReqVO reqVO, Integer userType) {
        // 1.1 幂等校验：根据 senderId + clientMessageId 查重
        ImPrivateMessageDO existing = privateMessageMapper.selectBySenderIdAndClientMessageId(
                senderId, reqVO.getClientMessageId());
        if (existing != null) {
            log.info("[sendPrivateMessage][幂等命中 senderId({}) clientMessageId({}) 已存在消息({})]",
                    senderId, reqVO.getClientMessageId(), existing.getId());
            return existing;
        }
        // 1.2 好友校验（暂时关闭，允许非好友聊天）
        // friendService.validateFriend(senderId, reqVO.getReceiverId());
        // 1.3 文本消息敏感词过滤
        if (ImMessageTypeEnum.TEXT.getType().equals(reqVO.getType())) {
            sensitiveWordService.validateText(reqVO.getContent());
        }

        // 2.1 引用 quote 消息规范化
        reqVO.setContent(normalizeQuoteContent(reqVO, senderId));
        // 2.2 构建并保存消息
        ImPrivateMessageDO message = BeanUtils.toBean(reqVO, ImPrivateMessageDO.class, m -> m
                .setSenderId(senderId).setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now()));
        privateMessageMapper.insert(message);

        // 3. WebSocket 异步推送：接收方 + 发送方多端同步
        ImPrivateMessageDTO websocketMessage = ImPrivateMessageDTO.ofSend(message);
        imWebSocketService.sendPrivateMessageAsync(userType, message.getReceiverId(), websocketMessage);
        imWebSocketService.sendPrivateMessageAsync(userType, senderId, websocketMessage);
        return message;
    }

    @Override
    public ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendDTO dto) {
        return sendPrivateMessage(senderId, dto, UserTypeEnum.ADMIN.getValue());
    }

    /**
     * 【系统调用】发送私聊消息（支持指定用户类型，无好友校验）
     * <p>
     * 用于 marriage 等业务模块绕过好友校验直接发送消息，由调用方自行控制聊天权限。
     *
     * @param senderId 发送人编号
     * @param dto      消息 DTO
     * @param userType 用户类型
     * @return 构造的消息 DO（持久化时 id 已回填）
     */
    public ImPrivateMessageDO sendPrivateMessage(Long senderId, ImPrivateMessageSendDTO dto, Integer userType) {
        // 1.1 content 序列化：null / String 透传，POJO 走 JSON
        Object payload = dto.getContent();
        String contentString = payload == null || payload instanceof String
                ? (String) payload
                : JsonUtils.toJsonString(payload);
        // 1.2 构建消息
        ImPrivateMessageDO message = new ImPrivateMessageDO().setClientMessageId(IdUtil.fastSimpleUUID())
                .setSenderId(senderId).setReceiverId(dto.getReceiverId())
                .setType(dto.getType()).setContent(contentString)
                .setStatus(ImMessageStatusEnum.UNREAD.getStatus()).setSendTime(LocalDateTime.now());
        // 1.3 决定是否持久化：dto.persistent 优先；为 null 时按 type 默认
        boolean persistent = dto.getPersistent() != null
                ? dto.getPersistent()
                : ImMessageTypeEnum.validate(dto.getType()).isPersistent();
        if (persistent) {
            privateMessageMapper.insert(message);
        }

        // 2. WebSocket 异步推送：双向（默认）；单边语义（persistent=false）下仅推 sender 多端，对方不感知
        ImPrivateMessageDTO websocketMessage = ImPrivateMessageDTO.ofSend(message);
        if (persistent) {
            imWebSocketService.sendPrivateMessageAsync(userType, dto.getReceiverId(), websocketMessage);
        }
        imWebSocketService.sendPrivateMessageAsync(userType, senderId, websocketMessage);
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImPrivateMessageDO recallPrivateMessage(Long userId, Long messageId) {
        // 1.1 校验消息存在
        ImPrivateMessageDO message = privateMessageMapper.selectById(messageId);
        if (message == null) {
            throw exception(MESSAGE_NOT_EXISTS);
        }
        // 1.2 只能撤回自己发送的消息
        if (ObjUtil.notEqual(message.getSenderId(), userId)) {
            throw exception(MESSAGE_RECALL_DENIED);
        }
        // 1.3 不能重复撤回
        if (ImMessageStatusEnum.RECALL.getStatus().equals(message.getStatus())) {
            throw exception(MESSAGE_ALREADY_RECALLED);
        }
        // 1.4 只允许撤回限定时间内的消息
        int recallTimeoutMinutes = imProperties.getMessage().getRecallTimeoutMinutes();
        if (message.getSendTime().plusMinutes(recallTimeoutMinutes).isBefore(LocalDateTime.now())) {
            throw exception(MESSAGE_RECALL_TIMEOUT, recallTimeoutMinutes);
        }

        // 2. 更新原消息状态为撤回
        privateMessageMapper.updateById(new ImPrivateMessageDO().setId(messageId)
                .setStatus(ImMessageStatusEnum.RECALL.getStatus()));

        // 3. 发送撤回事件
        return sendPrivateMessage(userId, new ImPrivateMessageSendDTO().setReceiverId(message.getReceiverId())
                .setType(ImMessageTypeEnum.RECALL.getType()).setContent(new RecallMessage().setMessageId(messageId)));
    }

    /**
     * 私聊引用消息规范化
     *
     * @param reqVO 发送请求
     * @param senderId 发送人编号
     * @return 规范化后的 content
     */
    private String normalizeQuoteContent(ImPrivateMessageSendReqVO reqVO, Long senderId) {
        // 解析客户端 content 里的 quote.messageId
        Long quoteMessageId = ImMessageUtils.parseQuoteMessageId(reqVO.getContent());

        // 情况一：没有 quoteMessageId，直接 remove 掉 content 里可能伪造的 quote 字段
        if (quoteMessageId == null) {
            return ImMessageUtils.removeQuote(reqVO.getContent());
        }

        // 情况二：有 quoteMessageId，加载原消息并校验
        ImPrivateMessageDO original = privateMessageMapper.selectById(quoteMessageId);
        if (original == null
                || ImMessageStatusEnum.RECALL.getStatus().equals(original.getStatus())) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 校验是同对话
        boolean sameConversation = (ObjUtil.equal(original.getSenderId(), senderId) // 发送人是当前用户，接收人是对方
                && ObjUtil.equal(original.getReceiverId(), reqVO.getReceiverId()))
                || (ObjUtil.equal(original.getSenderId(), reqVO.getReceiverId()) // 发送人是对方，接收人是当前用户
                        && ObjUtil.equal(original.getReceiverId(), senderId));
        if (!sameConversation) {
            throw exception(MESSAGE_QUOTE_INVALID);
        }
        // 构建 quote 对象并注入 content
        QuoteMessage quote = ImMessageUtils.buildQuote(original.getId(),
                original.getSenderId(), original.getType(), original.getContent());
        return ImMessageUtils.appendQuote(reqVO.getContent(), quote);
    }

    @Override
    public List<ImPrivateMessageDO> pullPrivateMessageList(Long userId, Long minId, Integer size) {
        int maxPullSize = imProperties.getMessage().getMaxPullSize();
        if (size > maxPullSize) {
            throw exception(MESSAGE_PULL_SIZE_EXCEEDED, maxPullSize);
        }
        // 0. 拉取时间窗；超过窗口的老消息不再通过离线通道推送
        LocalDateTime minSendTime = LocalDateTime.now().minusDays(imProperties.getMessage().getPrivatePullMaxDays());

        // 根据 minId 和 minSendTime 拉取消息，避免 minId 恰好被发出后才拉取，导致漏消息
        List<ImPrivateMessageDO> messages = privateMessageMapper.selectListByMinId(userId, minId, minSendTime, size);
        log.info("[pullPrivateMessageList][userId({}) minId({}) size({}) result({})]",
                userId, minId, size, messages.size());
        return messages;
    }

    @Override
    public void readPrivateMessages(Long userId, Long receiverId, Long messageId) {
        // 1. 全局开关校验
        if (BooleanUtil.isFalse(imProperties.getMessage().isPrivateReadEnabled())) {
            throw exception(MESSAGE_PRIVATE_READ_DISABLED);
        }
        Assert.notNull(messageId, "已读消息编号不能为空");
        // 2. 把 (receiverId → userId) 这条会话上、id <= messageId 的未读消息一步更新为已读
        // 仅 UNREAD 行被命中，避免覆盖已撤回/已读的状态；select-then-update 合成单条 SQL 后也消除了竞态窗口
        int updated = privateMessageMapper.updateBySenderIdAndReceiverIdAndIdLeAndStatus(
                receiverId, userId, messageId, ImMessageStatusEnum.UNREAD.getStatus(),
                new ImPrivateMessageDO().setStatus(ImMessageStatusEnum.READ.getStatus()));
        if (updated == 0) {
            return;
        }

        // 3. 异步发送 READ + RECEIPT 事件（已读位置以前端上报为准，与多端 / 对方 UI 显示一致）
        imWebSocketService.sendPrivateMessageAsync(userId,
                ImPrivateMessageDTO.ofRead(userId, receiverId, messageId));
        imWebSocketService.sendPrivateMessageAsync(receiverId,
                ImPrivateMessageDTO.ofReceipt(userId, receiverId, messageId));
    }

    @Override
    public Long getMaxReadMessageId(Long userId, Long peerId) {
        if (BooleanUtil.isFalse(imProperties.getMessage().isPrivateReadEnabled())) {
            throw exception(MESSAGE_PRIVATE_READ_DISABLED);
        }
        return privateMessageMapper.selectMaxIdBySenderIdAndReceiverIdAndStatus(
                userId, peerId, ImMessageStatusEnum.READ.getStatus());
    }

    @Override
    public List<ImPrivateMessageDO> getPrivateMessageList(Long userId, ImPrivateMessageListReqVO reqVO) {
        return privateMessageMapper.selectHistoryList(userId, reqVO.getReceiverId(), reqVO.getMaxId(), reqVO.getLimit());
    }

    // ==================== 管理后台 ====================

    @Override
    public PageResult<ImPrivateMessageDO> getPrivateMessagePage(ImPrivateMessageManagerPageReqVO reqVO) {
        return privateMessageMapper.selectPage(reqVO);
    }

    @Override
    public ImPrivateMessageDO getPrivateMessage(Long id) {
        return privateMessageMapper.selectById(id);
    }

    // ==================== App 端私信会话 ====================

    @Override
    public Long getUnreadCount(Long userId) {
        return privateMessageMapper.selectUnreadCount(userId);
    }

    @Override
    public List<ImConversationRespDTO> getConversationList(Long userId) {
        // 1. 查询会话列表（最近 30 天，最多 50 个会话）
        List<ImConversationBO> conversations = privateMessageMapper.selectConversationList(userId, 30, 50);

        // 2. 如果没有会话，返回空列表
        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 批量查询用户信息
        Set<Long> peerIds = conversations.stream()
                .map(ImConversationBO::getPeerId)
                .collect(Collectors.toSet());
        Map<Long, PartnerRespDTO> userMap = partnerApi.getPartnerMap(peerIds);

        // 4. 组装返回结果
        return conversations.stream().map(conv -> {
            PartnerRespDTO user = userMap.get(conv.getPeerId());
            return ImConversationRespDTO.builder()
                    .peerId(conv.getPeerId())
                    .peerNickname(user != null ? user.getNickname() : "未知用户")
                    .peerAvatar(user != null ? user.getAvatar() : "")
                    .lastMessage(parseLastMessage(conv.getLastMessageContent()))
                    .lastSendTime(conv.getLastSendTime())
                    .unreadCount(conv.getUnreadCount())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 解析消息内容（JSON 提取 text 字段，非 JSON 直接返回）
     */
    private String parseLastMessage(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        try {
            // 尝试解析 JSON 格式 {"text": "消息内容"}
            JsonNode jsonNode = objectMapper.readTree(content);
            if (jsonNode.has("text")) {
                return jsonNode.get("text").asText();
            }
        } catch (Exception e) {
            // 非 JSON 格式，直接返回原文
        }
        return content;
    }

}
