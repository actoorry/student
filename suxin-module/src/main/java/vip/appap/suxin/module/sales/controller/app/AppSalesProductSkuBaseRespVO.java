package vip.appap.suxin.module.sales.controller.app;

import vip.appap.suxin.module.sales.controller.app.AppSalesProductPropertyValueDetailRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 SKU 基础 Response VO
 *
 * @author 书心软件
 */
@Data
public class AppSalesProductSkuBaseRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "图片地址", example = "https://www.appap.vip/xx.png")
    private String picUrl;

    @Schema(description = "销售价格，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer price;

    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer stock;

    /**
     * 属性数组
     */
    private List<AppSalesProductPropertyValueDetailRespVO> properties;

    @Schema(description = "微信虚拟支付道具 ProductId", example = "product_1001")
    private String wechatVirtualProductId;

    @Schema(description = "微信虚拟支付道具发布状态", example = "2")
    private Integer wechatVirtualPublishStatus;

    @Schema(description = "微信虚拟支付道具审核状态", example = "2")
    private Integer wechatVirtualReviewStatus;

    @Schema(description = "微信虚拟支付道具审核失败原因", example = "名称不符合规范")
    private String wechatVirtualReviewFailReason;

    @Schema(description = "微信虚拟支付道具最后同步时间")
    private LocalDateTime wechatVirtualLastSyncTime;

}
