package vip.appap.suxin.module.accountant.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 微信虚拟支付提交 Response VO")
@Data
public class AppWechatVirtualPaySubmitRespVO {

    public static final String MODE_SHORT_SERIES_GOODS = "short_series_goods";

    @Schema(description = "支付订单编号", example = "1024")
    private Long payOrderId;

    @Schema(description = "支付扩展单号", example = "P202606291200001")
    private String payExtensionNo;

    @Schema(description = "微信虚拟支付模式", example = "short_series_goods")
    private String mode;

    @Schema(description = "参与签名且传给 wx.requestVirtualPayment 的 JSON 字符串")
    private String signData;

    @Schema(description = "支付签名")
    private String paySig;

    @Schema(description = "用户态签名")
    private String signature;

}
