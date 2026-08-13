package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 积分记录 Response VO")
@Data
public class AppPartnerPointRecordRespVO {

    @Schema(description = "记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "业务编码", example = "1")
    private String bizId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

    @Schema(description = "积分标题", example = "签到奖励")
    private String title;

    @Schema(description = "积分描述", example = "签到奖励 +10")
    private String description;

    @Schema(description = "变动积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer point;

    @Schema(description = "变动后积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalPoint;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
