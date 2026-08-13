package vip.appap.suxin.module.accountant.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 微信虚拟支付提交 Request VO")
@Data
public class AppWechatVirtualPaySubmitReqVO {

    @Schema(description = "支付订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "支付订单编号不能为空")
    private Long payOrderId;

    @Schema(description = "微信小程序 openid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "openid 不能为空")
    private String openid;

}
