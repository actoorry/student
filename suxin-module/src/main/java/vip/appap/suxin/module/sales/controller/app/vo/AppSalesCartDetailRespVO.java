package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductSkuBaseRespVO;
import vip.appap.suxin.module.sales.controller.app.AppSalesProductSpuBaseRespVO;

import java.util.List;

@Schema(description = "用户 App - 用户的购物车明细 Response VO")
@Data
public class AppSalesCartDetailRespVO {

    private List<ItemGroup> itemGroups;
    private Order order;

    @Schema(description = "商品分组")
    @Data
    public static class ItemGroup {

        private List<Sku> items;
        private Promotion promotion;

    }

    @Schema(description = "商品 SKU")
    @Data
    public static class Sku extends AppSalesProductSkuBaseRespVO {

        private AppSalesProductSpuBaseRespVO spu;

        @Schema(description = "商品数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        private Integer count;

        @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        private Boolean selected;

        private Integer originalPrice;
        private Integer totalOriginalPrice;
        private Integer totalPromotionPrice;
        private Integer totalPresentPrice;
        private Integer presentPrice;
        private Integer totalPayPrice;
        private Promotion promotion;

    }

    @Schema(description = "订单")
    @Data
    public static class Order {

        private Integer skuOriginalPrice;
        private Integer skuPromotionPrice;
        private Integer orderPromotionPrice;
        private Integer deliveryPrice;
        private Integer payPrice;

    }

    @Schema(description = "营销活动")
    @Data
    public static class Promotion {

        private Long id;
        private String name;
        private Integer type;
        private Boolean meet;
        private String meetTip;

    }

}
