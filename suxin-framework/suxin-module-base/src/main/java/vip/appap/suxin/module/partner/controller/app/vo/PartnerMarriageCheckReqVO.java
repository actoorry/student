package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "App - 婚姻认证 Request VO")
@Data
public class PartnerMarriageCheckReqVO {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "姓名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]+$", message = "真实姓名只能填写中文")
    private String name;

    @Schema(description = "身份证号", example = "110101199001010011")
    private String idCard;

}
