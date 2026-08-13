package vip.appap.suxin.module.crm.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.product.dal.dataobject.ProductSkuDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * CRM 商机产品关联表 DO
 *
 * CrmBusinessDO : CrmBusinessProductDO = 1 : N
 *
 * @author lzxhqs
 */
@TableName("crm_business_product")
@KeySequence("crm_business_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessProductDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 商机编号
     *
     * 关联 {@link CrmBusinessDO#getId()}
     */
    private Long businessId;
    /**
     * SKU 编号
     *
     * 关联 {@link ProductSkuDO#getId()}
     */
    private Long skuId;
    /**
     * 产品单价，单位：分
     *
     * 冗余 {@link ProductSkuDO#getPrice()}
     */
    private Integer productPrice;
    /**
     * 商机价格, 单位：分
     */
    private Integer businessPrice;
    /**
     * 数量
     */
    private BigDecimal count;
    /**
     * 总计价格，单位：分
     *
     * totalPrice = businessPrice * count
     */
    private Integer totalPrice;

}
