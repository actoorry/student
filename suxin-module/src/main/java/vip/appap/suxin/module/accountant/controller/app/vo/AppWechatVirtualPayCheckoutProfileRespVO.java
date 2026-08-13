package vip.appap.suxin.module.accountant.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 交易订单在小程序收银台应使用的支付通道画像。
 *
 * <p>该接口只返回路由结果和可展示原因，绝不返回虚拟支付 AppKey 等敏感配置。</p>
 */
@Schema(description = "用户 APP - 微信虚拟支付收银台画像 Response VO")
@Data
public class AppWechatVirtualPayCheckoutProfileRespVO {

    public static final String MODE_WECHAT_MERCHANT = "WECHAT_MERCHANT";
    public static final String MODE_WECHAT_VIRTUAL = "WECHAT_VIRTUAL";
    public static final String MODE_VIRTUAL_NOT_READY = "VIRTUAL_NOT_READY";

    @Schema(description = "支付订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long payOrderId;

    @Schema(description = "收银台模式", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "WECHAT_VIRTUAL")
    private String mode;

    @Schema(description = "虚拟支付时要求的支付渠道编码", example = "wx_virtual_lite")
    private String requiredChannelCode;

    @Schema(description = "不可发起虚拟支付时的展示原因", example = "微信虚拟支付道具尚未发布")
    private String unavailableReason;

}
