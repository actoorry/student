package vip.appap.suxin.module.rongjh.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 爱心帮扶审核 Request VO")
@Data
public class HelpAuditReqVO {

    @Schema(description = "申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "申请编号不能为空")
    private Long id;

    @Schema(description = "批准金额（审核通过时填写）", example = "5000.00")
    private BigDecimal actualAmount;

    @Schema(description = "审核备注")
    private String remark;

}
