package vip.appap.suxin.module.accountant.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 微信虚拟支付结果 Response VO")
@Data
public class AppWechatVirtualPayResultRespVO {

    @Schema(description = "支付订单状态")
    private Integer payStatus;

    @Schema(description = "微信虚拟支付发货状态")
    private Integer deliverStatus;

    @Schema(description = "支付扩展单号")
    private String payExtensionNo;

}
