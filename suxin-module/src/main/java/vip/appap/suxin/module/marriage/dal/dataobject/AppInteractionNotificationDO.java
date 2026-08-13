package vip.appap.suxin.module.marriage.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@TableName(value = "app_interaction_notification", autoResultMap = true)
@KeySequence("app_interaction_notification_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppInteractionNotificationDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * Business type, such as marriage.
     */
    private String bizType;

    /**
     * Business scene, such as FOLLOW.
     */
    private String scene;

    /**
     * Receiver member id.
     */
    private Long userId;

    /**
     * Actor member id. Empty for aggregate system events.
     */
    private Long actorId;

    private String title;

    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    /**
     * Business idempotency key.
     */
    private String bizKey;

    private Boolean readStatus;

    private LocalDateTime readTime;

    private LocalDateTime eventTime;

}
