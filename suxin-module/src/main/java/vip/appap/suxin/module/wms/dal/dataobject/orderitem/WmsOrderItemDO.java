package vip.appap.suxin.module.wms.dal.dataobject.orderitem;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 出入库/调拨单据明细 DO
 *
 * @author admin
 */
@TableName("wms_order_item")
@KeySequence("wms_order_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOrderItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 出入库单ID，FK -> wms_order.id
     */
    private Long orderId;
    /**
     * 产品SKU ID，FK -> product_sku.id
     */
    private Long skuId;
    /**
     * 来源明细ID（如purchase_order_item.id），用于回写已收数量
     */
    private Long originId;
    /**
     * 来源位置。实体库位或虚拟仓ID
     */
    private Long fromLocationId;
    /**
     * 去向位置。实体库位或虚拟仓ID
     */
    private Long toLocationId;
    /**
     * 批次号。stock_mode≠0时必填，入库手动录入，出库按FIFO自动匹配
     */
    private String batchNo;
    /**
     * 生产日期
     */
    private LocalDate productionDate;
    /**
     * 有效期至
     */
    private LocalDate expiryDate;
    /**
     * 移动数量（绝对值）。入库=入库量，出库=出库量
     */
    private BigDecimal quantity;
    /**
     * 单价，仅入库时可选填写
     */
    private BigDecimal unitPrice;
    /**
     * 行排序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;


}
