package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "App - 合作伙伴微信小程序登录 Request VO")
@Data
public class PartnerWxMiniLoginReqVO {

    @Schema(description = "微信小程序登录 code", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "code 不能为空")
    private String code;

    @Schema(description = "微信昵称", example = "张三")
    private String nickname;

    @Schema(description = "微信头像", example = "https://thirdwx.qlogo.cn/mmopen/xxx/132")
    private String avatarUrl;

    @Schema(description = "手机号授权 code", example = "xxx")
    private String phoneCode;

    @Schema(description = "閭€璇蜂汉缁戝畾鐨勫垎閿€鐢ㄦ埛缂栧彿", example = "10001")
    private Long bindUserId;

}
