package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vip.appap.suxin.framework.common.util.validation.ValidationUtils;
import vip.appap.suxin.module.accountant.framework.pay.core.client.PayClientConfig;

/**
 * 微信小程序虚拟支付配置。
 */
@Data
public class WxVirtualPayClientConfig implements PayClientConfig {

    /**
     * 小程序 appid。
     */
    @NotBlank(message = "APPID 不能为空")
    private String appid;

    /**
     * 虚拟支付 offer_id。
     */
    @NotBlank(message = "offerId 不能为空")
    private String offerId;

    /**
     * 环境：0 现网，1 沙箱。
     */
    @NotNull(message = "env 不能为空")
    private Integer env;

    /**
     * 与 env 匹配的 AppKey。
     */
    @NotBlank(message = "AppKey 不能为空")
    private String appKey;

    @Override
    public void validate(Validator validator) {
        ValidationUtils.validate(validator, this);
    }

}
