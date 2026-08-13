package vip.appap.suxin.module.product.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.framework.mybatis.core.type.IntegerListTypeHandler;
import vip.appap.suxin.module.product.dal.dataobject.ProductBrandDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductCategoryDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import vip.appap.suxin.module.product.enums.ProductSpuStatusEnum;
import vip.appap.suxin.module.product.enums.ProductTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.util.List;

/**
 * 商品 SPU DO
 *
 * @author 书心软件
 */
@TableName(value = "product_spu", autoResultMap = true)
@KeySequence("product_spu_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpuDO extends BaseDO {

    /**
     * 商品 SPU 编号，自增
     */
    @TableId
    private Long id;

    // ========== 基本信息 =========

    /**
     * 商品名称
     */
    private String name;
    /**
     * 关键字
     */
    private String keyword;
    /**
     * 商品简介
     */
    private String introduction;
    /**
     * 商品详情
     */
    private String description;

    /**
     * 销售分类ID
     *
     * 关联 {@link ProductCategoryDO#getId()}
     */
    @TableField("category_sales")
    private Long categorySales;
    /**
     * 仓储分类ID
     */
    private Long categoryStore;
    /**
     * 商品品牌编号
     *
     * 关联 {@link ProductBrandDO#getId()}
     */
    private Long brandId;
    /**
     * 产品单位编号
     *
     * 关联 {@link vip.appap.suxin.module.product.dal.dataobject.ProductUnitDO#getId()}
     */
    private Long unitId;
    /**
     * 商品封面图
     */
    private String picUrl;
    /**
     * 商品轮播图
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> sliderPicUrls;

    /**
     * 排序字段
     */
    private Integer sort;
    /**
     * 商品状态
     *
     * 枚举 {@link ProductSpuStatusEnum}
     */
    private Integer status;

    // ========== ERP 控制字段 ==========

    /**
     * 产品类型
     *
     * 枚举 {@link ProductTypeEnum}
     */
    private Integer type;
    /**
     * 是否微信小程序虚拟商品。创建后不可通过常规更新接口修改。
     */
    private Boolean isWechatMiniappVirtualGoods;
    /**
     * 是否可销售
     */
    private Boolean isSale;
    /**
     * 是否可采购
     */
    private Boolean isPurchase;
    /**
     * 是否 MES 管理
     */
    private Boolean isMes;
    /**
     * 是否军创区商品
     *
     * 仅用于军创区展示范围筛选，不作为商品类型、配送方式、库存或履约判断依据
     */
    private Boolean isMilitary;
    /**
     * 库存管控方式：0=不管理（默认） 1=批次管理 2=序列号管理
     */
    @TableField("stock_mode")
    private Integer stockMode;

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
     *
     * 基于其对应的 {@link ProductSkuDO#getPrice()} sku单价最低的商品的
     */
    private Integer price;
    /**
     * 市场价，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getMarketPrice()} sku单价最低的商品的
     */
    private Integer marketPrice;
    /**
     * 成本价，单位使用：分
     *
     * 基于其对应的 {@link ProductSkuDO#getCostPrice()} sku单价最低的商品的
     */
    private Integer costPrice;
    /**
     * 库存
     *
     * 基于其对应的 {@link ProductSkuDO#getStock()} 求和
     */
    private Integer stock;

    // ========== 物流相关字段 =========

    /**
     * 配送方式数组
     *
     * 对应 SalesDeliveryTypeEnum 枚举
     */
    @TableField(typeHandler = IntegerListTypeHandler.class)
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

    // TODO @puhui999：字段估计要改成 brokerageType
    /**
     * 分销类型
     *
     * false - 默认
     * true - 自行设置
     */
    private Boolean subCommissionType;

    // ========== 统计相关字段 =========

    /**
     * 商品销量
     */
    private Integer salesCount;
    /**
     * 虚拟销量
     */
    private Integer virtualSalesCount;
    /**
     * 浏览量
     */
    private Integer browseCount;

    /**
     * 归属城市/区域编号（同城特产、按省筛选）
     */
    private Integer cityId;
}
