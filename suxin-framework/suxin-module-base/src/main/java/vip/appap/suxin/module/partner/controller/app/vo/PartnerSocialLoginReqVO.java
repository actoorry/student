package vip.appap.suxin.module.partner.controller.app.vo;

import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.module.system.enums.SocialTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "App - 合作伙伴社交登录 Request VO")
@Data
public class PartnerSocialLoginReqVO {

    @Schema(description = "社交平台的类型，参见 SocialTypeEnum 枚举值", requiredMode = Schema.RequiredMode.REQUIRED, example = "34")
    @NotNull(message = "社交平台的类型不能为空")
    @InEnum(SocialTypeEnum.class)
    private Integer type;

    @Schema(description = "授权码", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotEmpty(message = "授权码不能为空")
    private String code;

    @Schema(description = "state", example = "9b2ffbc1-7425-4155-9894-9d5c08541d62")
    private String state;

}
