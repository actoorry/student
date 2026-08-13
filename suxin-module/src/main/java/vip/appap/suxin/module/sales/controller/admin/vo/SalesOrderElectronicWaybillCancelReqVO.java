package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理后台 - 电子面单取消 Request VO")
@Data
public class SalesOrderElectronicWaybillCancelReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "取消原因", example = "客户修改地址")
    @Size(max = 255, message = "取消原因长度不能超过 255")
    private String reason;

}
