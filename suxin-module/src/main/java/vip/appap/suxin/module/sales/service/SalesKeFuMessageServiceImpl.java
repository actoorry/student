package vip.appap.suxin.module.sales.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.infra.api.websocket.WebSocketSenderApi;
import vip.appap.suxin.module.partner.api.PartnerApi;
import vip.appap.suxin.module.partner.api.dto.PartnerRespDTO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageListReqVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageRespVO;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuMessageSendReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesKeFuMessagePageReqVO;
import vip.appap.suxin.module.sales.controller.app.vo.AppSalesKeFuMessageSendReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuConversationDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesKeFuMessageMapper;
import vip.appap.suxin.module.system.api.AdminUserApi;
import vip.appap.suxin.module.system.api.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.*;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.KEFU_CONVERSATION_NOT_EXISTS;
import static vip.appap.suxin.module.sales.enums.WebSocketMessageTypeConstants.KEFU_MESSAGE_ADMIN_READ;
import static vip.appap.suxin.module.sales.enums.WebSocketMessageTypeConstants.KEFU_MESSAGE_TYPE;

/**
 * 客服消息 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesKeFuMessageServiceImpl implements SalesKeFuMessageService {

    @Resource
    private SalesKeFuMessageMapper keFuMessageMapper;
    @Resource
    private SalesKeFuConversationService conversationService;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private PartnerApi PartnerApi;
    @Resource
    private WebSocketSenderApi webSocketSenderApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendKefuMessage(SalesKeFuMessageSendReqVO sendReqVO) {
        // 1.1 校验会话是否存在
        SalesKeFuConversationDO conversation = conversationService.validateKefuConversationExists(sendReqVO.getConversationId());
        // 1.2 校验接收人是否存在
        validateReceiverExist(conversation.getUserId(), UserTypeEnum.MEMBER.getValue());

        // 2.1 保存消息
        SalesKeFuMessageDO kefuMessage = BeanUtils.toBean(sendReqVO, SalesKeFuMessageDO.class);
        kefuMessage.setReceiverId(conversation.getUserId()).setReceiverType(UserTypeEnum.MEMBER.getValue()); // 设置接收人
        keFuMessageMapper.insert(kefuMessage);
        // 2.2 更新会话消息冗余
        conversationService.updateConversationLastMessage(kefuMessage);

        // 3.1 发送消息给会员
        AdminUserRespDTO user = adminUserApi.getUser(kefuMessage.getSenderId());
        SalesKeFuMessageRespVO message = BeanUtils.toBean(kefuMessage, SalesKeFuMessageRespVO.class).setSenderAvatar(user.getAvatar());
        getSelf().sendAsyncMessageToPartner(conversation.getUserId(), KEFU_MESSAGE_TYPE, message);
        // 3.2 通知所有管理员更新对话
        getSelf().sendAsyncMessageToAdmin(KEFU_MESSAGE_TYPE, message);
        return kefuMessage.getId();
    }

    @Override
    public Long sendKefuMessage(AppSalesKeFuMessageSendReqVO sendReqVO) {
        // 1.1 设置会话编号
        SalesKeFuMessageDO kefuMessage = BeanUtils.toBean(sendReqVO, SalesKeFuMessageDO.class);
        SalesKeFuConversationDO conversation = conversationService.getOrCreateConversation(sendReqVO.getSenderId());
        kefuMessage.setConversationId(conversation.getId());
        // 1.2 保存消息
        keFuMessageMapper.insert(kefuMessage);

        // 2. 更新会话消息冗余
        conversationService.updateConversationLastMessage(kefuMessage);
        // 3. 通知所有管理员更新对话
        PartnerRespDTO user = PartnerApi.getUser(kefuMessage.getSenderId());
        SalesKeFuMessageRespVO message = BeanUtils.toBean(kefuMessage, SalesKeFuMessageRespVO.class).setSenderAvatar(user.getAvatar());
        getSelf().sendAsyncMessageToAdmin(KEFU_MESSAGE_TYPE, message);
        getSelf().sendAsyncMessageToPartner(conversation.getUserId(), KEFU_MESSAGE_TYPE, message);
        return kefuMessage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateKeFuMessageReadStatus(Long conversationId, Long userId, Integer userType) {
        // 1.1 校验会话是否存在
        SalesKeFuConversationDO conversation = conversationService.validateKefuConversationExists(conversationId);
        // 1.2 如果是会员端处理已读，需要传递 userId；万一用户模拟一个 conversationId
        if (UserTypeEnum.MEMBER.getValue().equals(userType) && ObjUtil.notEqual(conversation.getUserId(), userId)) {
            throw exception(KEFU_CONVERSATION_NOT_EXISTS);
        }
        // 1.3 查询会话所有的未读消息 (tips: 多个客服，一个人点了，就都点了)
        List<SalesKeFuMessageDO> messageList = keFuMessageMapper.selectListByConversationIdAndUserTypeAndReadStatus(conversationId, userType, Boolean.FALSE);
        if (CollUtil.isEmpty(messageList)) {
            return;
        }

        // 2.1 情况二：更新未读消息状态为已读
        keFuMessageMapper.updateReadStatusBatchByIds(convertSet(messageList, SalesKeFuMessageDO::getId),
                new SalesKeFuMessageDO().setReadStatus(Boolean.TRUE));
        // 2.2 将管理员未读消息计数更新为零
        conversationService.updateAdminUnreadMessageCountToZero(conversationId);

        // 2.3 发送消息通知会员，管理员已读 -> 会员更新发送的消息状态
        SalesKeFuMessageDO keFuMessage = getFirst(filterList(messageList, message -> UserTypeEnum.MEMBER.getValue().equals(message.getSenderType())));
        assert keFuMessage != null; // 断言避免警告
        getSelf().sendAsyncMessageToPartner(keFuMessage.getSenderId(), KEFU_MESSAGE_ADMIN_READ,
                new SalesKeFuMessageRespVO().setConversationId(keFuMessage.getConversationId()));
        // 2.4 通知所有管理员消息已读
        getSelf().sendAsyncMessageToAdmin(KEFU_MESSAGE_ADMIN_READ,
                new SalesKeFuMessageRespVO().setConversationId(keFuMessage.getConversationId()));
    }

    private void validateReceiverExist(Long receiverId, Integer receiverType) {
        if (UserTypeEnum.ADMIN.getValue().equals(receiverType)) {
            adminUserApi.validateUser(receiverId);
        }
        if (UserTypeEnum.MEMBER.getValue().equals(receiverType)) {
            PartnerApi.validateUser(receiverId);
        }
    }

    @Async
    public void sendAsyncMessageToPartner(Long userId, String messageType, Object content) {
        webSocketSenderApi.sendObject(UserTypeEnum.MEMBER.getValue(), userId, messageType, content);
    }

    @Async
    public void sendAsyncMessageToAdmin(String messageType, Object content) {
        webSocketSenderApi.sendObject(UserTypeEnum.ADMIN.getValue(), messageType, content);
    }

    @Override
    public List<SalesKeFuMessageDO> getKeFuMessageList(SalesKeFuMessageListReqVO pageReqVO) {
        return keFuMessageMapper.selectList(pageReqVO);
    }

    @Override
    public List<SalesKeFuMessageDO> getKeFuMessageList(AppSalesKeFuMessagePageReqVO pageReqVO, Long userId) {
        // 1. 获得客服会话
        SalesKeFuConversationDO conversation = conversationService.getConversationByUserId(userId);
        if (conversation == null) {
            return Collections.emptyList();
        }
        // 2. 设置会话编号
        pageReqVO.setConversationId(conversation.getId());
        return keFuMessageMapper.selectList(BeanUtils.toBean(pageReqVO, SalesKeFuMessageListReqVO.class));
    }

    private SalesKeFuMessageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}