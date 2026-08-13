package vip.appap.suxin.module.marriage.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 婚恋装修可观测事件类型
 */
@Getter
@AllArgsConstructor
public enum MarriageDecorationEventType {

    CONFIG_RETRIEVAL_FAILED("config_retrieval_failed", "配置获取失败"),
    CONFIG_INVALID("config_invalid", "配置不兼容"),
    CACHE_REPLACED("cache_replaced", "装修缓存替换"),
    ROUTE_REJECTED("route_rejected", "装修路由拒绝"),
    UNREGISTERED_COMPONENT("unregistered_component", "未注册装修组件"),
    BLOCK_RENDER_FAILED("block_render_failed", "组件渲染失败"),
    FALLBACK_ACTIVATED("fallback_activated", "回退到原生布局");

    private final String code;
    private final String description;

}
