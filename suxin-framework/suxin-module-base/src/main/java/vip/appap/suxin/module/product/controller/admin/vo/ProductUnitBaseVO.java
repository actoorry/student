package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 产品单位 Base VO
 */
@Schema(description = "管理后台 - 产品单位 Base VO")
@Data
public class ProductUnitBaseVO {

    @Schema(description = "单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    @NotBlank(message = "单位名称不能为空")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "单位类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "单位类型不能为空")
    private Integer type;

    @Schema(description = "换算系数", example = "1000")
    private String relativeFactor;

}
