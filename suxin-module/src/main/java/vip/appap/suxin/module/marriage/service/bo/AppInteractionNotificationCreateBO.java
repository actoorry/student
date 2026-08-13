package vip.appap.suxin.module.marriage.service.bo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 互动通知创建的服务层输入。
 */
@Data
@Accessors(chain = true)
public class AppInteractionNotificationCreateBO {

    private String bizType;
    private String scene;
    private Long userId;
    private Long actorId;
    private String title;
    private String content;
    private Map<String, Object> payload;
    private String bizKey;
    private LocalDateTime eventTime;

}
