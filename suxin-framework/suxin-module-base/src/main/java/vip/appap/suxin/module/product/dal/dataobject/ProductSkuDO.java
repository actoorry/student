package vip.appap.suxin.module.product.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductPropertyDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductPropertyValueDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSpuDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 SKU DO
 *
 * @author 书心软件
 */
@TableName(value = "product_sku", autoResultMap = true)
@KeySequence("product_sku_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDO extends BaseDO {

    /**
     * 商品 SKU 编号，自增
     */
    @TableId
    private Long id;
    /**
     * SPU 编号
     *
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;
    /**
     * 属性数组，JSON 格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Property> properties;
    /**
     * 商品价格，单位：分
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
     * 商品条码
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
    @TableField("min_stock")
    private BigDecimal minStock;
    /**
     * 安全库存上限，高于此值触发积压预警
     */
    @TableField("max_stock")
    private BigDecimal maxStock;

    /**
     * 一级分销的佣金，单位：分
     */
    private Integer firstBrokeragePrice;
    /**
     * 二级分销的佣金，单位：分
     */
    private Integer secondBrokeragePrice;

    // ========== 微信小程序虚拟支付道具字段 =========
    /**
     * 微信虚拟支付道具 ProductId。
     */
    private String wechatVirtualProductId;
    /**
     * 微信虚拟支付道具上传任务编号。
     */
    private String wechatVirtualUploadTaskId;
    /**
     * 微信虚拟支付道具上传状态。
     */
    private Integer wechatVirtualUploadStatus;
    /**
     * 微信虚拟支付道具发布任务编号。
     */
    private String wechatVirtualPublishTaskId;
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

    // ========== 营销相关字段 =========

    // ========== 统计相关字段 =========
    /**
     * 商品销量
     */
    private Integer salesCount;

    /**
     * 商品属性
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {

        /**
         * 属性编号
         * 关联 {@link ProductPropertyDO#getId()}
         */
        private Long propertyId;
        /**
         * 属性名字
         * 冗余 {@link ProductPropertyDO#getName()}
         *
         * 注意：每次属性名字发生变化时，需要更新该冗余
         */
        private String propertyName;

        /**
         * 属性值编号
         * 关联 {@link ProductPropertyValueDO#getId()}
         */
        private Long valueId;
        /**
         * 属性值名字
         * 冗余 {@link ProductPropertyValueDO#getName()}
         *
         * 注意：每次属性值名字发生变化时，需要更新该冗余
         */
        private String valueName;

    }

}

