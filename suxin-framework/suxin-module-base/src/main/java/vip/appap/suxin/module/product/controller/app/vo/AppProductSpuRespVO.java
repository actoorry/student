package vip.appap.suxin.module.product.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 商品 SPU Response VO")
@Data
public class AppProductSpuRespVO {

    @Schema(description = "商品 SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "书心")
    private String name;

    @Schema(description = "商品简介", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖简介")
    private String introduction;

    @Schema(description = "销售分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categorySales;

    @Schema(description = "商品封面图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String picUrl;

    @Schema(description = "商品轮播图", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> sliderPicUrls;

    // ========== SKU 相关字段 =========

    @Schema(description = "规格类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean specType;

    @Schema(description = "商品价格，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer price;

    @Schema(description = "市场价，单位使用：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer marketPrice;

    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "666")
    private Integer stock;

    // ========== 营销相关字段 =========

    // ========== 统计相关字段 =========

    @Schema(description = "商品销量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer salesCount;

    @Schema(description = "是否微信小程序虚拟商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isWechatMiniappVirtualGoods;

    // ========== 物流相关字段 =========

    @Schema(description = "配送方式数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private List<Integer> deliveryTypes;

    @Schema(description = "是否军创区商品", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean isMilitary;

}
