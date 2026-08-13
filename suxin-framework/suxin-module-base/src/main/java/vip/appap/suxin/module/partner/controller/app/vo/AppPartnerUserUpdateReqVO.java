package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 APP - 修改个人信息 Request VO")
@Data
public class AppPartnerUserUpdateReqVO {

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotNull(message = "用户昵称不能为空")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.appap.vip/avatar.png")
    private String avatar;

    @Schema(description = "用户性别", example = "1")
    private Integer gender;

}
