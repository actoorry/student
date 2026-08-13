package vip.appap.suxin.module.im.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * App 端 - 私信会话 Response VO。
 */
@Schema(description = "App 端 - 私信会话 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppImConversationRespVO {

    @Schema(description = "对方用户编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long peerId;
    @Schema(description = "对方昵称")
    private String peerNickname;
    @Schema(description = "对方头像 URL")
    private String peerAvatar;
    @Schema(description = "最后一条消息内容")
    private String lastMessage;
    @Schema(description = "最后消息发送时间")
    private LocalDateTime lastSendTime;
    @Schema(description = "未读消息数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer unreadCount;

}
