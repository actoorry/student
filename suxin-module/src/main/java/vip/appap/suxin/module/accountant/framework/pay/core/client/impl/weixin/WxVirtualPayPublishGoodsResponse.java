package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信虚拟支付批量发布道具响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxVirtualPayPublishGoodsResponse extends WxVirtualPayCommonResponse {

    // start_publish_goods 仅返回 errcode 和 errmsg

}
