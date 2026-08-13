package vip.appap.suxin.module.sales.service;

import vip.appap.suxin.module.product.service.ProductDeliveryTemplateValidator;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 商品快递配送模板校验端口实现
 *
 * 位于 sales 边界，复用 {@link SalesDeliveryExpressTemplateService} 校验当前租户模板
 * 存在、未删除且至少有一条计费规则；供下层 product 通过 {@link ProductDeliveryTemplateValidator}
 * 调用，形成依赖倒置且不产生 Maven 环。
 */
@Component
public class SalesDeliveryExpressTemplateValidator implements ProductDeliveryTemplateValidator {

    @Resource
    private SalesDeliveryExpressTemplateService deliveryExpressTemplateService;

    @Override
    public void validateDeliveryTemplate(Long templateId) {
        deliveryExpressTemplateService.validateDeliveryExpressTemplateComputable(templateId);
    }

}
