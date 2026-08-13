package vip.appap.suxin.module.partner.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "App - 婚姻认证状态 Response VO")
@Data
public class PartnerMarriageCheckStatusRespVO {

    @Schema(description = "是否存在历史认证结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean checked;

    @Schema(description = "最近一次是否成功拿到认证结果", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean success;

    @Schema(description = "婚姻状态", example = "2")
    private String state;

    @Schema(description = "婚姻状态文案", example = "未婚")
    private String stateLabel;

    @Schema(description = "最近一次结果说明", example = "婚姻认证结果：未婚")
    private String reason;

    @Schema(description = "本月认证次数是否已达上限", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean limitReached;

    @Schema(description = "下次可认证日期")
    private LocalDate nextAvailableDate;

    @Schema(description = "限额提示")
    private String limitMessage;

}
