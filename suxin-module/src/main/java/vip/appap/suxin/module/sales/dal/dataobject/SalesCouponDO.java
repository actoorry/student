package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.framework.mybatis.core.type.LongListTypeHandler;
import vip.appap.suxin.module.sales.enums.SalesDiscountTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesProductScopeEnum;
import vip.appap.suxin.module.sales.enums.SalesCouponStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesCouponTakeTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠劵 DO
 *
 * @author 书心软件
 */
@TableName(value = "sales_coupon", autoResultMap = true)
@KeySequence("sales_coupon_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class SalesCouponDO extends BaseDO {

    // ========== 基本信息 BEGIN ==========
    /**
     * 优惠劵编号
     */
    @TableId
    private Long id;
    /**
     * 优惠劵模板编号
     *
     * 关联 {@link SalesCouponTemplateDO#getId()}
     */
    private Long templateId;
    /**
     * 优惠劵名
     *
     * 冗余 {@link SalesCouponTemplateDO#getName()}
     */
    private String name;
    /**
     * 优惠码状态
     *
     * 枚举 {@link SalesCouponStatusEnum}
     */
    private Integer status;

    // TODO 芋艿：发放 adminid？

    // ========== 基本信息 END ==========

    // ========== 领取情况 BEGIN ==========
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 领取类型
     *
     * 枚举 {@link SalesCouponTakeTypeEnum}
     */
    private Integer takeType;
    // ========== 领取情况 END ==========

    // ========== 使用规则 BEGIN ==========
    /**
     * 是否设置满多少金额可用，单位：分
     *
     * 冗余 {@link SalesCouponTemplateDO#getUsePrice()}
     */
    private Integer usePrice;
    /**
     * 生效开始时间
     */
    private LocalDateTime validStartTime;
    /**
     * 生效结束时间
     */
    private LocalDateTime validEndTime;
    /**
     * 商品范围
     *
     * 枚举 {@link SalesProductScopeEnum}
     */
    private Integer productScope;
    /**
     * 商品范围编号的数组
     *
     * 冗余 {@link SalesCouponTemplateDO#getProductScopeValues()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> productScopeValues;
    // ========== 使用规则 END ==========

    // ========== 使用效果 BEGIN ==========
    /**
     * 折扣类型
     *
     * 冗余 {@link SalesCouponTemplateDO#getDiscountType()}
     */
    private Integer discountType;
    /**
     * 折扣百分比
     *
     * 冗余 {@link SalesCouponTemplateDO#getDiscountPercent()}
     */
    private Integer discountPercent;
    /**
     * 优惠金额，单位：分
     *
     * 冗余 {@link SalesCouponTemplateDO#getDiscountPrice()}
     */
    private Integer discountPrice;
    /**
     * 折扣上限，仅在 {@link #discountType} 等于 {@link SalesDiscountTypeEnum#PERCENT} 时生效
     *
     * 冗余 {@link SalesCouponTemplateDO#getDiscountLimitPrice()}
     */
    private Integer discountLimitPrice;
    // ========== 使用效果 END ==========

    // ========== 使用情况 BEGIN ==========
    /**
     * 使用订单号
     */
    private Long useOrderId;
    /**
     * 使用时间
     */
    private LocalDateTime useTime;

    // ========== 使用情况 END ==========

}
