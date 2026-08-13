package vip.appap.suxin.module.im.mq.consumer.friend;

import vip.appap.suxin.module.im.dal.dataobject.ImFriendDO;
import vip.appap.suxin.module.im.enums.ImMessageTypeEnum;
import vip.appap.suxin.module.im.service.ImFriendService;
import vip.appap.suxin.module.im.service.websocket.ImWebSocketService;
import vip.appap.suxin.module.im.service.websocket.dto.ImPrivateMessageDTO;
import vip.appap.suxin.module.im.service.websocket.dto.notification.friend.FriendInfoUpdatedNotification;
import vip.appap.suxin.module.partner.api.PartnerProfileUpdateMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 监听 partner 模块的 {@link PartnerProfileUpdateMessage} 消息，向「资料被改的人」的所有好友推送 FRIEND_INFO_UPDATED 通知
 *
 * @author 书心软件
 */
@Slf4j
@Component
public class AdminUserProfileUpdateConsumer {

    @Resource
    private ImFriendService friendService;
    @Resource
    private ImWebSocketService websocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async // Spring Event 默认在 Producer 发送的线程，通过 @Async 实现异步；事务提交后触发，避免回滚误推幽灵通知 / Consumer 抢在 commit 前读旧值
    public void onMessage(PartnerProfileUpdateMessage message) {
        log.info("[onMessage][消息内容({})]", message);
        Long userId = message.getUserId();
        // 1. SQL 层过滤 ENABLE 好友
        List<ImFriendDO> friends = friendService.getEnableFriendList(userId);
        if (friends.isEmpty()) {
            return;
        }

        // 2. 给每个好友的多端推 FRIEND_INFO_UPDATED；payload 里 operatorUserId / friendUserId 都是「资料被改的人」
        for (ImFriendDO friend : friends) {
            FriendInfoUpdatedNotification payload = (FriendInfoUpdatedNotification) new FriendInfoUpdatedNotification()
                    .setOperatorUserId(userId).setFriendUserId(userId);
            websocketService.sendPrivateMessageAsync(friend.getFriendUserId(), ImPrivateMessageDTO.ofFriendNotification(
                    ImMessageTypeEnum.FRIEND_INFO_UPDATED.getType(), userId, friend.getFriendUserId(), payload));
        }
        log.info("[onMessage][userId({}) 推送 FRIEND_INFO_UPDATED 给 {} 位好友]", userId, friends.size());
    }

}
