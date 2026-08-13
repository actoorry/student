package vip.appap.suxin.module.product.service;

/**
 * 商品快递配送模板校验端口
 *
 * 定义在 product 所在的 {@code suxin-module-base} 边界，只表达调用方（商品服务）的需求，
 * 不引用上层 sales 类型，不泄漏 sales DO。由上层 sales 模块实现本接口完成依赖倒置，
 * 避免 product 直接依赖 sales 形成 Maven 反向依赖。
 *
 * 校验不通过时抛出 {@link vip.appap.suxin.framework.common.exception.ServiceException}。
 */
public interface ProductDeliveryTemplateValidator {

    /**
     * 校验运费模板可用于快递结算（存在、当前租户、未删除且至少有一条计费规则）
     *
     * @param templateId 运费模板编号
     */
    void validateDeliveryTemplate(Long templateId);

}
