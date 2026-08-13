package vip.appap.suxin.module.partner.controller.app.vo;

import vip.appap.suxin.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Schema(description = "App - 合作伙伴短信登录 Request VO")
@Data
public class PartnerSmsLoginReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @NotEmpty(message = "手机号不能为空")
    @Mobile
    private String mobile;

    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为 6 位数字")
    private String code;

}
