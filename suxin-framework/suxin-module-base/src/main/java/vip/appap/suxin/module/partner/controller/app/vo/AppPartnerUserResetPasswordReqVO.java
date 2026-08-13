package vip.appap.suxin.module.partner.controller.app.vo;

import vip.appap.suxin.framework.common.validation.Mobile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "用户 APP - 重置密码 Request VO")
@Data
public class AppPartnerUserResetPasswordReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @NotEmpty(message = "手机号不能为空")
    @Mobile
    private String mobile;

    @Schema(description = "短信验证码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    @NotEmpty(message = "短信验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为 6 位数字")
    private String code;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "654321")
    @NotEmpty(message = "新密码不能为空")
    private String password;

}
