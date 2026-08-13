package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "用户 APP - 微信小程序修改手机号 Request VO")
@Data
public class AppPartnerUserUpdateMobileByWeixinReqVO {

    @Schema(description = "微信授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "xxx")
    @NotEmpty(message = "微信授权码不能为空")
    private String code;

}
