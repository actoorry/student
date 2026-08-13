package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 商品 SKU 精简信息 Response VO")
@Data
public class ProductSkuSimpleRespVO {

    @Schema(description = "SKU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long spuId;

    @Schema(description = "产品名称（来自 SPU）", requiredMode = Schema.RequiredMode.REQUIRED, example = "苹果")
    private String spuName;

    @Schema(description = "商品条码", example = "BAR001")
    private String barCode;

    @Schema(description = "商品价格，单位：分", example = "9900")
    private Integer price;

    @Schema(description = "库存", example = "100")
    private Integer stock;

    @Schema(description = "主单位数量", example = "30")
    private BigDecimal quantity;

    @Schema(description = "商品状态（来自 SPU）", example = "1")
    private Integer status;

    @Schema(description = "产品单位编号", example = "1")
    private Long unitId;

    @Schema(description = "产品单位名称", example = "个")
    private String unitName;

}
