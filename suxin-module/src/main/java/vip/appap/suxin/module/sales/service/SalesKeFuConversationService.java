package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesKeFuConversationUpdatePinnedReqVO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuConversationDO;
import vip.appap.suxin.module.sales.dal.dataobject.SalesKeFuMessageDO;

import java.util.List;

/**
 * 客服会话 Service 接口
 *
 * @author HUIHUI
 */
public interface SalesKeFuConversationService {

    /**
     * 获得客服会话
     *
     * @param id 编号
     * @return 客服会话
     */
    SalesKeFuConversationDO getConversation(Long id);

    /**
     * 【管理员】删除客服会话
     *
     * @param id 编号
     */
    void deleteKefuConversation(Long id);

    /**
     * 【管理员】客服会话置顶
     *
     * @param updateReqVO 请求
     */
    void updateConversationPinnedByAdmin(SalesKeFuConversationUpdatePinnedReqVO updateReqVO);

    /**
     * 更新会话客服消息冗余信息
     *
     * @param kefuMessage 消息
     */
    void updateConversationLastMessage(SalesKeFuMessageDO kefuMessage);

    /**
     * 【管理员】将管理员未读消息计数更新为零
     *
     * @param id 编号
     */
    void updateAdminUnreadMessageCountToZero(Long id);

    /**
     * 【管理员】更新会话对于管理员是否可见
     *
     * @param id           编号
     * @param adminDeleted 管理员是否可见
     */
    void updateConversationAdminDeleted(Long id, Boolean adminDeleted);

    /**
     * 【管理员】获得客服会话列表
     *
     * @return 会话列表
     */
    List<SalesKeFuConversationDO> getKefuConversationList();

    /**
     * 【会员】获得或创建会话
     *
     * 对于【会员】来说，有且仅有一个对话
     *
     * @param userId 用户编号
     * @return 客服会话
     */
    SalesKeFuConversationDO getOrCreateConversation(Long userId);

    /**
     * 校验客服会话是否存在
     *
     * @param id 编号
     * @return 客服会话
     */
    SalesKeFuConversationDO validateKefuConversationExists(Long id);

    /**
     * 【会员】获得客服会话
     *
     * @param userId 用户编号
     * @return 客服会话
     */
    SalesKeFuConversationDO getConversationByUserId(Long userId);

}