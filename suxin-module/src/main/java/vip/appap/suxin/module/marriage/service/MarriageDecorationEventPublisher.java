package vip.appap.suxin.module.marriage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 婚恋装修非 PII 可观测事件发布器
 */
@Slf4j
@Component
public class MarriageDecorationEventPublisher {

    public void publish(MarriageDecorationEventType type, Long tenantId, Long templateId,
                        String pageName, String componentId, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", type.getCode());
        event.put("eventDesc", type.getDescription());
        if (tenantId != null) {
            event.put("tenantId", tenantId);
        }
        if (templateId != null) {
            event.put("templateId", templateId);
        }
        if (pageName != null) {
            event.put("pageName", pageName);
        }
        if (componentId != null) {
            event.put("componentId", componentId);
        }
        if (reason != null) {
            event.put("reason", reason);
        }
        log.info("[MarriageDecorationEvent] {}", event);
    }

}
