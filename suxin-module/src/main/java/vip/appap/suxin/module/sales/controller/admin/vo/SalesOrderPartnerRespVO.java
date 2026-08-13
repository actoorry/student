package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 销售订单合作方（买家） Response VO")
@Data
public class SalesOrderPartnerRespVO {

    @Schema(description = "用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "书心软件")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.appap.vip/xxx.png")
    private String avatar;

}
