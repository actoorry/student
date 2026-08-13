package vip.appap.suxin.module.rongjh.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 帮扶累计金额 Response VO")
@Data
public class AppHelpTotalAmountRespVO {

    @Schema(description = "我的爱心贡献值（累计有效消费金额 / 100）")
    @JsonProperty("contribution_amount")
    private BigDecimal contributionAmount;

    @Schema(description = "平台帮扶公示累计金额（与 contribution_amount 同源，兼容 Odoo 字段）")
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "统计截止日期")
    private String deadline;

}
