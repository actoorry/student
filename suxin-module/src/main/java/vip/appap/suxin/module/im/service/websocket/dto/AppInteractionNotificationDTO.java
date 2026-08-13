package vip.appap.suxin.module.im.service.websocket.dto;

import vip.appap.suxin.module.marriage.dal.dataobject.AppInteractionNotificationDO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AppInteractionNotificationDTO {

    public static final String TYPE = "app-interaction-notification";

    private Long id;

    private String bizType;

    private String scene;

    private Long userId;

    private Long actorId;

    private String title;

    private String content;

    private Map<String, Object> payload;

    private Boolean readStatus;

    private LocalDateTime eventTime;

    public static AppInteractionNotificationDTO of(AppInteractionNotificationDO notification) {
        return AppInteractionNotificationDTO.builder()
                .id(notification.getId())
                .bizType(notification.getBizType())
                .scene(notification.getScene())
                .userId(notification.getUserId())
                .actorId(notification.getActorId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .payload(notification.getPayload())
                .readStatus(notification.getReadStatus())
                .eventTime(notification.getEventTime())
                .build();
    }

}
