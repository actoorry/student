package vip.appap.suxin.module.product.api.dto;

import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * 商品 SPU 信息 Response DTO
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
public class ProductSpuRespDTO {

    /**
     * 商品 SPU 编号，自增
     */
    private Long id;

    // ========== 基本信息 =========

    /**
     * 商品名称
     */
    private String name;

    /**
     * 销售分类ID
     */
    private Long categorySales;
    /**
     * 商品封面图
     */
    private String picUrl;

    /**
     * 商品状态
     * <p>
     * 枚举 {@link ProductSpuStatusEnum}
     */
    private Integer status;
    /**
     * 商品类型
     */
    private Integer type;
    /**
     * 库存管控方式：0=不管理（默认） 1=批次管理 2=序列号管理
     */
    private Integer stockMode;

    /**
     * 是否微信小程序虚拟商品
     */
    private Boolean isWechatMiniappVirtualGoods;

    // ========== SKU 相关字段 =========

    /**
     * 规格类型
     *
     * false - 单规格
     * true - 多规格
     */
    private Boolean specType;
    /**
     * 商品价格，单位使用：分
     */
    private Integer price;
    /**
     * 市场价，单位使用：分
     */
    private Integer marketPrice;
    /**
     * 成本价，单位使用：分
     */
    private Integer costPrice;
    /**
     * 库存
     */
    private Integer stock;

    // ========== 物流相关字段 =========

    /**
     * 配送方式数组
     *
     * 对应 SalesDeliveryTypeEnum 枚举
     */
    private List<Integer> deliveryTypes;

    /**
     * 物流配置模板编号
     *
     * 对应 SalesDeliveryExpressTemplateDO 的 id 编号
     */
    private Long deliveryTemplateId;

    // ========== 营销相关字段 =========

    /**
     * 赠送积分
     */
    private Integer giveIntegral;

    // ========== 分销相关字段 =========

    /**
     * 分销类型
     *
     * false - 默认
     * true - 自行设置
     */
    private Boolean subCommissionType;

}
