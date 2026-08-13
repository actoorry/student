package vip.appap.suxin.module.product.api.dto;

import vip.appap.suxin.module.product.api.dto.ProductPropertyValueDetailRespDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 SKU 信息 Response DTO
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
public class ProductSkuRespDTO {

    /**
     * 商品 SKU 编号，自增
     */
    private Long id;
    /**
     * SPU 编号
     */
    private Long spuId;

    /**
     * 属性数组
     */
    private List<ProductPropertyValueDetailRespDTO> properties;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 市场价，单位：分
     */
    private Integer marketPrice;
    /**
     * 成本价，单位：分
     */
    private Integer costPrice;
    /**
     * SKU 的条形码
     */
    private String barCode;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 库存
     */
    private Integer stock;
    /**
     * 商品重量，单位：kg 千克
     */
    private Double weight;
    /**
     * 商品体积，单位：m^3 平米
     */
    private Double volume;
    /**
     * 主单位数量
     */
    private BigDecimal quantity;
    /**
     * 安全库存下限，低于此值触发缺货预警
     */
    private BigDecimal minStock;
    /**
     * 安全库存上限，高于此值触发积压预警
     */
    private BigDecimal maxStock;
    /**
     * 一级分销的佣金，单位：分
     */
    private Integer firstBrokeragePrice;
    /**
     * 二级分销的佣金，单位：分
     */
    private Integer secondBrokeragePrice;
    /**
     * 微信虚拟支付道具 ProductId。
     */
    private String wechatVirtualProductId;
    /**
     * 微信虚拟支付道具上传状态。
     */
    private Integer wechatVirtualUploadStatus;
    /**
     * 微信虚拟支付道具发布状态。
     */
    private Integer wechatVirtualPublishStatus;
    /**
     * 微信虚拟支付道具审核状态。
     */
    private Integer wechatVirtualReviewStatus;
    /**
     * 微信虚拟支付道具审核失败原因。
     */
    private String wechatVirtualReviewFailReason;
    /**
     * 微信虚拟支付道具最后同步时间。
     */
    private LocalDateTime wechatVirtualLastSyncTime;

}
