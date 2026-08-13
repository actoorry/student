package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 会员配置 Response VO")
@Data
public class AppPartnerMemberConfigRespVO {

    @Schema(description = "会员类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer memberType;

    @Schema(description = "微信小程序会员商品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long memberCategoryId;

}
