package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "App - 合作伙伴登录 Response VO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLoginRespVO {

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
    private String accessToken;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
    private String refreshToken;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.appap.vip/xxx.png")
    private String avatar;

}
