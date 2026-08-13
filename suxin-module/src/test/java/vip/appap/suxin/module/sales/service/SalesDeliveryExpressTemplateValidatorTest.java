package vip.appap.suxin.module.sales.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import vip.appap.suxin.module.product.service.ProductDeliveryTemplateValidator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SalesDeliveryExpressTemplateValidator} 端口装配契约测试
 *
 * 证明 product 位于下层 {@code suxin-module-base}，通过无环端口调用上层 sales 实现：
 * 1. sales 实现类实现了下层端口（无 Maven 反向依赖，base 不 import sales）；
 * 2. 实现类标注 {@link Component}，完整应用启动时会被扫描装配，不会因缺少实现而静默跳过校验。
 */
public class SalesDeliveryExpressTemplateValidatorTest {

    @Test
    @DisplayName("sales 实现类实现了 product 端口")
    public void implementsProductDeliveryTemplateValidator() {
        assertTrue(ProductDeliveryTemplateValidator.class.isAssignableFrom(
                SalesDeliveryExpressTemplateValidator.class));
    }

    @Test
    @DisplayName("实现类标注 @Component，可被 Spring 扫描装配")
    public void isSpringComponent() {
        assertNotNull(SalesDeliveryExpressTemplateValidator.class.getAnnotation(Component.class),
                "缺少 @Component 会导致完整应用无实现，商品校验被静默跳过");
    }

}
