package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import lombok.Getter;

/**
 * 微信虚拟支付接口返回的业务错误异常。
 */
@Getter
public class WxVirtualPayApiException extends RuntimeException {

    private final Integer errcode;

    public WxVirtualPayApiException(Integer errcode, String errmsg) {
        super(errmsg);
        this.errcode = errcode;
    }

}
