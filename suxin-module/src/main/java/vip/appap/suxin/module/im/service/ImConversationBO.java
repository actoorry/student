package vip.appap.suxin.module.im.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私信会话的 Mapper 到 Service 内部模型。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImConversationBO {

    private Long peerId;
    private String lastMessageContent;
    private LocalDateTime lastSendTime;
    private Integer unreadCount;

}
