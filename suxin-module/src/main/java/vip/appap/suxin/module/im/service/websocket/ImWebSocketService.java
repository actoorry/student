package vip.appap.suxin.module.im.service.websocket;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.module.im.service.websocket.dto.ImChannelMessageDTO;
import vip.appap.suxin.module.im.service.websocket.dto.ImGroupMessageDTO;
import vip.appap.suxin.module.im.service.websocket.dto.ImPrivateMessageDTO;
import vip.appap.suxin.module.im.service.websocket.dto.AppInteractionNotificationDTO;

import java.util.Collection;
import java.util.Collections;

/**
 * IM WebSocket 推送 Service 接口
 * <p>
 * 统一封装 WebSocket 消息推送，所有方法默认异步执行。
 *
 * @author 芋道源码
 */
public interface ImWebSocketService {

    /**
     * 通过 Spring 异步代理发送私聊消息。
     *
     * <p>该方法必须保留在接口上：{@code ImWebSocketService} 在启用方法校验时可能使用 JDK 动态代理，
     * 只有接口方法才能通过代理调用 {@code @Async} 实现。</p>
     */
    void doSendPrivateMessage(Integer userType, Collection<Long> userIds, ImPrivateMessageDTO dto);

    void doSendGroupMessage(Integer userType, Collection<Long> userIds, ImGroupMessageDTO dto);

    void doSendChannelMessage(Integer userType, Collection<Long> userIds, ImChannelMessageDTO dto);

    void doBroadcastChannelMessage(ImChannelMessageDTO dto);

    void doSendAppInteractionNotification(Integer userType, Collection<Long> userIds,
                                          AppInteractionNotificationDTO dto);

    /**
     * 异步推送私聊消息给指定用户（默认 ADMIN 类型，兼容现有调用）
     *
     * @param userId 目标用户编号
     * @param dto    私聊消息 DTO
     */
    default void sendPrivateMessageAsync(Long userId, ImPrivateMessageDTO dto) {
        sendPrivateMessageAsync(UserTypeEnum.ADMIN.getValue(), userId, dto);
    }

    /**
     * 异步推送私聊消息给指定用户（支持指定用户类型）
     *
     * @param userType 用户类型（{@link UserTypeEnum}）
     * @param userId   目标用户编号
     * @param dto      私聊消息 DTO
     */
    default void sendPrivateMessageAsync(Integer userType, Long userId, ImPrivateMessageDTO dto) {
        sendPrivateMessageAsync(userType, Collections.singleton(userId), dto);
    }

    /**
     * 异步批量推送私聊消息给多个用户（默认 ADMIN 类型，兼容现有调用）
     *
     * @param userIds 目标用户编号列表
     * @param dto     私聊消息 DTO
     */
    default void sendPrivateMessageAsync(Collection<Long> userIds, ImPrivateMessageDTO dto) {
        sendPrivateMessageAsync(UserTypeEnum.ADMIN.getValue(), userIds, dto);
    }

    /**
     * 异步批量推送私聊消息给多个用户（支持指定用户类型）
     * <p>
     * 相比逐个 sendPrivateMessageAsync，仅注册一个 afterCommit 回调 + 一个 @Async 任务，大群参与者事件（1602 / 1603）下避免 N 次线程池调度
     *
     * @param userType 用户类型（{@link UserTypeEnum}）
     * @param userIds  目标用户编号列表
     * @param dto      私聊消息 DTO
     */
    void sendPrivateMessageAsync(Integer userType, Collection<Long> userIds, ImPrivateMessageDTO dto);

    /**
     * 异步推送群聊消息给指定用户（默认 ADMIN 类型，兼容现有调用）
     *
     * @param userId 目标用户编号
     * @param dto    群聊消息 DTO
     */
    default void sendGroupMessageAsync(Long userId, ImGroupMessageDTO dto) {
        sendGroupMessageAsync(UserTypeEnum.ADMIN.getValue(), userId, dto);
    }

    /**
     * 异步推送群聊消息给指定用户（支持指定用户类型）
     *
     * @param userType 用户类型（{@link UserTypeEnum}）
     * @param userId   目标用户编号
     * @param dto      群聊消息 DTO
     */
    default void sendGroupMessageAsync(Integer userType, Long userId, ImGroupMessageDTO dto) {
        sendGroupMessageAsync(userType, Collections.singleton(userId), dto);
    }

    /**
     * 异步批量推送群聊消息给多个用户（默认 ADMIN 类型，兼容现有调用）
     *
     * @param userIds 目标用户编号列表
     * @param dto     群聊消息 DTO
     */
    default void sendGroupMessageAsync(Collection<Long> userIds, ImGroupMessageDTO dto) {
        sendGroupMessageAsync(UserTypeEnum.ADMIN.getValue(), userIds, dto);
    }

    /**
     * 异步批量推送群聊消息给多个用户（支持指定用户类型）
     *
     * @param userType 用户类型（{@link UserTypeEnum}）
     * @param userIds  目标用户编号列表
     * @param dto      群聊消息 DTO
     */
    void sendGroupMessageAsync(Integer userType, Collection<Long> userIds, ImGroupMessageDTO dto);

    /**
     * 异步推送频道消息给指定用户（默认 ADMIN 类型，兼容现有调用）
     *
     * @param userId 目标用户编号
     * @param dto    频道消息 DTO
     */
    default void sendChannelMessageAsync(Long userId, ImChannelMessageDTO dto) {
        sendChannelMessageAsync(UserTypeEnum.ADMIN.getValue(), userId, dto);
    }

    /**
     * 异步推送频道消息给指定用户（支持指定用户类型）
     *
     * @param userType 用户类型（{@link UserTypeEnum}）
     * @param userId   目标用户编号
     * @param dto      频道消息 DTO
     */
    default void sendChannelMessageAsync(Integer userType, Long userId, ImChannelMessageDTO dto) {
        sendChannelMessageAsync(userType, Collections.singleton(userId), dto);
    }

    /**
     * 异步批量推送频道消息给多个用户（默认 ADMIN 类型，兼容现有调用）
     *
     * @param userIds 目标用户编号列表
     * @param dto     频道消息 DTO
     */
    default void sendChannelMessageAsync(Collection<Long> userIds, ImChannelMessageDTO dto) {
        sendChannelMessageAsync(UserTypeEnum.ADMIN.getValue(), userIds, dto);
    }

    /**
     * 异步批量推送频道消息给多个用户（支持指定用户类型）
     *
     * @param userType 用户类型（{@link UserTypeEnum}）
     * @param userIds  目标用户编号列表
     * @param dto      频道消息 DTO
     */
    void sendChannelMessageAsync(Integer userType, Collection<Long> userIds, ImChannelMessageDTO dto);

    /**
     * 异步广播频道消息给当前所有在线管理端用户；用于全员推送
     *
     * @param dto 频道消息 DTO
     */
    void broadcastChannelMessageAsync(ImChannelMessageDTO dto);

    default void sendAppInteractionNotificationAsync(Integer userType, Long userId,
                                                     AppInteractionNotificationDTO dto) {
        sendAppInteractionNotificationAsync(userType, Collections.singleton(userId), dto);
    }

    void sendAppInteractionNotificationAsync(Integer userType, Collection<Long> userIds,
                                             AppInteractionNotificationDTO dto);

}
