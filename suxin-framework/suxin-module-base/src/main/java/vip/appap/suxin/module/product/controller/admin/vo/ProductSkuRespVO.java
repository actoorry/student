package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 商品 SKU Response VO")
@Data
public class ProductSkuRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "商品 SKU 名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "清凉小短袖")
    private String name;

    @Schema(description = "销售价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1999")
    private Integer price;

    @Schema(description = "市场价", example = "2999")
    private Integer marketPrice;

    @Schema(description = "成本价", example = "19")
    private Integer costPrice;

    @Schema(description = "条形码", example = "15156165456")
    private String barCode;

    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/xx.png")
    private String picUrl;

    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer stock;

    @Schema(description = "商品重量,单位：kg 千克", example = "1.2")
    private Double weight;

    @Schema(description = "商品体积,单位：m^3 平米", example = "2.5")
    private Double volume;

    @Schema(description = "主单位数量", example = "30")
    private BigDecimal quantity;

    @Schema(description = "一级分销的佣金，单位：分", example = "199")
    private Integer firstBrokeragePrice;

    @Schema(description = "二级分销的佣金，单位：分", example = "19")
    private Integer secondBrokeragePrice;

    @Schema(description = "属性数组")
    private List<ProductSkuSaveReqVO.Property> properties;

    @Schema(description = "微信虚拟支付道具 ProductId", example = "product_1001")
    private String wechatVirtualProductId;

    @Schema(description = "微信虚拟支付道具上传任务编号", example = "upload_1001")
    private String wechatVirtualUploadTaskId;

    @Schema(description = "微信虚拟支付道具上传状态", example = "2")
    private Integer wechatVirtualUploadStatus;

    @Schema(description = "微信虚拟支付道具发布任务编号", example = "publish_1001")
    private String wechatVirtualPublishTaskId;

    @Schema(description = "微信虚拟支付道具发布状态", example = "2")
    private Integer wechatVirtualPublishStatus;

    @Schema(description = "微信虚拟支付道具审核状态", example = "2")
    private Integer wechatVirtualReviewStatus;

    @Schema(description = "微信虚拟支付道具审核失败原因", example = "名称不符合规范")
    private String wechatVirtualReviewFailReason;

    @Schema(description = "微信虚拟支付道具最后同步时间")
    private LocalDateTime wechatVirtualLastSyncTime;

}
