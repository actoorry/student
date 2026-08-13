package vip.appap.suxin.module.im.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私信会话的服务层输出。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImConversationRespDTO {

    private Long peerId;
    private String peerNickname;
    private String peerAvatar;
    private String lastMessage;
    private LocalDateTime lastSendTime;
    private Integer unreadCount;

}
