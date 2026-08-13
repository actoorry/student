package vip.appap.suxin.module.im.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App 端 - 私聊消息 Response VO
 */
@Schema(description = "App 端 - 私聊消息 Response VO")
@Data
public class AppImPrivateMessageRespVO {

    @Schema(description = "消息编号", example = "1")
    private Long id;

    @Schema(description = "客户端消息编号", example = "uuid-xxx")
    private String clientMessageId;

    @Schema(description = "发送人编号", example = "1")
    private Long senderId;

    @Schema(description = "接收人编号", example = "2")
    private Long receiverId;

    @Schema(description = "消息类型", example = "1")
    private Integer type;

    @Schema(description = "消息内容", example = "{\"text\":\"你好\"}")
    private String content;

    @Schema(description = "消息状态", example = "0")
    private Integer status;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

}
