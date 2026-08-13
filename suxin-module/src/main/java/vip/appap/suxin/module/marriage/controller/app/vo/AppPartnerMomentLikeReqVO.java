package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 动态点赞 Request VO")
@Data
public class AppPartnerMomentLikeReqVO {

    @Schema(description = "动态 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "动态 ID 不能为空")
    private Long momentId;

}
