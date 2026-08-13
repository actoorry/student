package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 订单电子面单发货 Request VO")
@Data
public class SalesOrderElectronicWaybillDeliveryReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "电子面单账户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "电子面单账户不能为空")
    private Long accountId;

    @Schema(description = "寄件地址编号（为空时使用账户默认寄件地址）", example = "1")
    private Long addressId;

    @Schema(description = "打印类型：HTML / IMAGE", example = "HTML")
    @NotBlank(message = "打印类型不能为空")
    private String printType;

}
