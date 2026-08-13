package vip.appap.suxin.module.accountant.framework.pay.core.client.impl.weixin;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 微信虚拟支付批量上传道具响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxVirtualPayUploadGoodsResponse extends WxVirtualPayCommonResponse {

    // start_upload_goods 仅返回 errcode 和 errmsg

}
