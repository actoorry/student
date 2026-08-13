package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 分销用户信息 Response VO")
@Data
public class AppSalesBrokerageUserRespVO {

    @Schema(description = "是否有分销资格", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean brokerageEnabled;

    @Schema(description = "分销用户记录是否存在", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean brokerageUserExists;

    @Schema(description = "可用的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "2408")
    private Integer brokeragePrice;

    @Schema(description = "冻结的佣金，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "234")
    private Integer frozenPrice;

}
