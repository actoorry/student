package vip.appap.suxin.module.product.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SKU 微信虚拟支付道具同步状态。
 */
@Getter
@AllArgsConstructor
public enum ProductSkuWechatVirtualStatusEnum {

    NONE(0, "未同步"),
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败");

    private final Integer status;
    private final String name;

}
