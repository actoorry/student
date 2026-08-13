package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductSkuBaseRespVO;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductSpuBaseRespVO;

import java.util.List;

@Schema(description = "用户 App - 用户的购物车列表 Response VO")
@Data
public class AppSalesCartListRespVO {

    @Schema(description = "有效购物项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Cart> validList;

    @Schema(description = "无效购物项数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Cart> invalidList;

    @Schema(description = "购物项")
    @Data
    public static class Cart {

        @Schema(description = "购物项编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer count;

        @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean selected;

        @Schema(description = "商品 SPU", requiredMode = Schema.RequiredMode.REQUIRED)
        private AppSalesProductSpuBaseRespVO spu;

        @Schema(description = "商品 SKU", requiredMode = Schema.RequiredMode.REQUIRED)
        private AppSalesProductSkuBaseRespVO sku;

        @Schema(description = "不可结算原因")
        private String invalidReason;

    }

}
