package vip.appap.suxin.module.partner.controller.app.vo;

import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.framework.common.validation.Mobile;
import vip.appap.suxin.module.system.enums.SmsSceneEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(description = "App - 合作伙伴发送短信验证码 Request VO")
@Data
public class PartnerSmsSendReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @Mobile
    @NotEmpty(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "发送场景", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "发送场景不能为空")
    @InEnum(SmsSceneEnum.class)
    private Integer scene;

}
