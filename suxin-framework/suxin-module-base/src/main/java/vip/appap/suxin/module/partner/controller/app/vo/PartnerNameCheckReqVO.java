package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "App - 实名认证 Request VO")
@Data
public class PartnerNameCheckReqVO {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 20, message = "姓名不能超过 20 个字符")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]+$", message = "真实姓名只能填写中文")
    private String name;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110101199001010011")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

}
