package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "App - 实名认证创建 Request VO")
@Data
public class MarriageRealAuthCreateReqVO {

    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED, example = "110101199003074512")
    @NotBlank(message = "身份证号不能为空")
    private String idCardNo;

    @Schema(description = "身份证正面照片URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/idcard_front.png")
    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFrontImage;

    @Schema(description = "身份证背面照片URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/idcard_back.png")
    @NotBlank(message = "身份证背面照片不能为空")
    private String idCardBackImage;

    @Schema(description = "手持身份证照片URL", example = "https://www.appap.vip/selfie.png")
    private String selfieImage;

}
