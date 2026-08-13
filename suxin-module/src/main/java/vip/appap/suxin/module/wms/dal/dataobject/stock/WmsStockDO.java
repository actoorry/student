package vip.appap.suxin.module.wms.dal.dataobject.stock;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库存快照 DO
 *
 * @author admin
 */
@TableName("wms_stock")
@KeySequence("wms_stock_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsStockDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品SKU ID，FK -> product_sku.id
     */
    private Long skuId;
    /**
     * 所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）
     */
    private Long warehouseId;
    /**
     * 存储库位ID，FK -> wms_warehouse.id（叶子节点）
     */
    private Long locationId;
    /**
     * 库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）
     */
    private Integer stockMode;
    /**
     * 批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号
     */
    private String batchNo;
    /**
     * 生产日期，批次管理时使用
     */
    private LocalDate productionDate;
    /**
     * 有效期至
     */
    private LocalDate expiryDate;
    /**
     * 库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）
     */
    private BigDecimal quantity;
    /**
     * 预占数量。提交审核时预占，审核通过时扣减，取消时解占
     */
    private BigDecimal reservedQuantity;
    /**
     * 最近入库单价（移动平均），仅入库类单据更新
     */
    private BigDecimal unitPrice;


}
