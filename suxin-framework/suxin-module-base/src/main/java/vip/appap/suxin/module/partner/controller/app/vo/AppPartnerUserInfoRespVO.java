package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 用户个人信息 Response VO")
@Data
public class AppPartnerUserInfoRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/avatar.png")
    private String avatar;

    @Schema(description = "用户手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    private String mobile;

    @Schema(description = "用户性别", example = "1")
    private Integer gender;

    @Schema(description = "积分", example = "100")
    private Integer point;

    @Schema(description = "会员等级编号", example = "1")
    private Long customerLevel;

    @Schema(description = "是否会员", example = "true")
    private Boolean isMember;

    @Schema(description = "注册终端", example = "1")
    private Integer registerTerminal;

}
