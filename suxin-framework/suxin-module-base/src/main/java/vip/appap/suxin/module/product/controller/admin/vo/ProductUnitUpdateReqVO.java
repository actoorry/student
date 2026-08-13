package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 产品单位更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductUnitUpdateReqVO extends ProductUnitBaseVO {

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "单位编号不能为空")
    private Long id;

}
