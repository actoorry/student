package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 产品单位精简信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitSimpleRespVO {

    @Schema(description = "单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "单位名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "个")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "单位类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer type;

    @Schema(description = "换算系数", example = "1000")
    private String relativeFactor;

}
