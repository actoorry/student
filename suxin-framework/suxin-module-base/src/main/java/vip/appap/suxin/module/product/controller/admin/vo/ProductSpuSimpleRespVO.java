package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Schema(description = "管理后台 - 商品 SPU 精简 Response VO")
@Data
@ToString(callSuper = true)
public class ProductSpuSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "213")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖")
    private String name;

    @Schema(description = "产品类型: 1-实体产品, 2-服务产品, 3-组合产品, 4-会员产品", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "是否微信小程序虚拟商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isWechatMiniappVirtualGoods;

    @Schema(description = "商品价格，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer price;

    @Schema(description = "商品市场价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "199")
    private Integer marketPrice;

    @Schema(description = "商品成本价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "19")
    private Integer costPrice;

    @Schema(description = "商品库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer stock;

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer salesCount;

    @Schema(description = "商品虚拟销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20000")
    private Integer virtualSalesCount;

    @Schema(description = "商品浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2000")
    private Integer browseCount;

}
