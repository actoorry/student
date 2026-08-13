package vip.appap.suxin.module.sales.service;

import cn.hutool.core.util.StrUtil;
import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuConversationUpdatePinnedReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuConversationDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;
import vip.appap.suxin.module.sales.dal.mysql.SalesKeFuConversationMapper;
import vip.appap.suxin.module.sales.enums.SalesKeFuMessageContentTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.sales.enums.SalesErrorCodeConstants.KEFU_CONVERSATION_NOT_EXISTS;

/**
 * 客服会话 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class SalesKeFuConversationServiceImpl implements SalesKeFuConversationService {

    @Resource
    private SalesKeFuConversationMapper conversationMapper;

    @Override
    public SalesKeFuConversationDO getConversation(Long id) {
        return conversationMapper.selectById(id);
    }

    @Override
    public void deleteKefuConversation(Long id) {
        // 校验存在
        validateKefuConversationExists(id);

        // 只有管理员端可以删除会话，也不真的删，只是管理员端看不到啦
        conversationMapper.updateById(new SalesKeFuConversationDO().setId(id).setAdminDeleted(Boolean.TRUE));
    }

    @Override
    public void updateConversationPinnedByAdmin(SalesKeFuConversationUpdatePinnedReqVO updateReqVO) {
        // 校验存在
        validateKefuConversationExists(updateReqVO.getId());

        // 更新管理员会话置顶状态
        conversationMapper.updateById(new SalesKeFuConversationDO().setId(updateReqVO.getId()).setAdminPinned(updateReqVO.getAdminPinned()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConversationLastMessage(SalesKeFuMessageDO kefuMessage) {
        // 1.1 校验会话是否存在
        SalesKeFuConversationDO conversation = validateKefuConversationExists(kefuMessage.getConversationId());
        // 1.2 更新会话消息冗余
        conversationMapper.updateById(new SalesKeFuConversationDO().setId(kefuMessage.getConversationId())
                .setLastMessageTime(kefuMessage.getCreateTime()).setLastMessageContent(kefuMessage.getContent())
                .setLastMessageContentType(kefuMessage.getContentType()));

        // 2.1 更新管理员未读消息数
        if (UserTypeEnum.MEMBER.getValue().equals(kefuMessage.getSenderType())) {
            conversationMapper.updateAdminUnreadMessageCountIncrement(kefuMessage.getConversationId());
        }
        // 2.2 会员用户发送消息时，如果管理员删除过会话则进行恢复
        if (Boolean.TRUE.equals(conversation.getAdminDeleted())) {
            updateConversationAdminDeleted(kefuMessage.getConversationId(), Boolean.FALSE);
        }
    }

    @Override
    public void updateAdminUnreadMessageCountToZero(Long id) {
        // 校验存在
        validateKefuConversationExists(id);

        // 管理员未读消息数归零
        conversationMapper.updateById(new SalesKeFuConversationDO().setId(id).setAdminUnreadMessageCount(0));
    }

    @Override
    public void updateConversationAdminDeleted(Long id, Boolean adminDeleted) {
        conversationMapper.updateById(new SalesKeFuConversationDO().setId(id).setAdminDeleted(adminDeleted));
    }

    @Override
    public List<SalesKeFuConversationDO> getKefuConversationList() {
        return conversationMapper.selectConversationList();
    }

    @Override
    public SalesKeFuConversationDO getOrCreateConversation(Long userId) {
        SalesKeFuConversationDO conversation = conversationMapper.selectOne(SalesKeFuConversationDO::getUserId, userId);
        // 没有历史会话，则初始化一个新会话
        if (conversation == null) {
            conversation = new SalesKeFuConversationDO().setUserId(userId).setLastMessageTime(LocalDateTime.now())
                    .setLastMessageContent(StrUtil.EMPTY).setLastMessageContentType(SalesKeFuMessageContentTypeEnum.TEXT.getType())
                    .setAdminPinned(Boolean.FALSE).setUserDeleted(Boolean.FALSE).setAdminDeleted(Boolean.FALSE)
                    .setAdminUnreadMessageCount(0);
            conversationMapper.insert(conversation);
        }
        return conversation;
    }

    @Override
    public SalesKeFuConversationDO validateKefuConversationExists(Long id) {
        SalesKeFuConversationDO conversation = conversationMapper.selectById(id);
        if (conversation == null) {
            throw exception(KEFU_CONVERSATION_NOT_EXISTS);
        }
        return conversation;
    }

    @Override
    public SalesKeFuConversationDO getConversationByUserId(Long userId) {
        return conversationMapper.selectByUserId(userId);
    }

}