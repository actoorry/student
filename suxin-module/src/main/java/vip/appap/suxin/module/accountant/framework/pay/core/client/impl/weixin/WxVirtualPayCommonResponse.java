package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import lombok.Data;

/**
 * 微信虚拟支付接口通用响应。
 */
@Data
public class WxVirtualPayCommonResponse {

    /**
     * 错误码，0 表示成功
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    public boolean isSuccess() {
        return errcode != null && errcode == 0;
    }

}
