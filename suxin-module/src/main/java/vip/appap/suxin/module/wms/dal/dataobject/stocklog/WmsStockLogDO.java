package vip.appap.suxin.module.wms.dal.dataobject.stocklog;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 库存流水台账 DO
 *
 * 注意：流水表仅追加，不做逻辑删除，故屏蔽 BaseDO 中表不存在的字段
 *
 * @author admin
 */
@TableName("wms_stock_log")
@KeySequence("wms_stock_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class WmsStockLogDO extends BaseDO {

    /**
     * 屏蔽 BaseDO 的逻辑删除字段（流水表不删除，仅追加）
     */
    @TableField(exist = false)
    private Boolean deleted;

    /**
     * 屏蔽 BaseDO 的更新人字段（流水表无此列）
     */
    @TableField(exist = false)
    private String updater;

    /**
     * 屏蔽 BaseDO 的更新时间字段（流水表无此列）
     */
    @TableField(exist = false)
    private LocalDateTime updateTime;

    /**
     * 屏蔽 BaseDO 的创建者字段（流水表无此列）
     */
    @TableField(exist = false)
    private String creator;
    

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 产品SKU ID
     */
    private Long skuId;
    /**
     * 实体仓库ID
     */
    private Long warehouseId;
    /**
     * 来源位置（虚拟仓或实体库位）
     */
    private Long fromLocationId;
    /**
     * 去向位置（虚拟仓或实体库位）
     */
    private Long toLocationId;
    /**
     * 批次号
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
     * 来源业务：order=出入库单 / adjust=盘点调整单
     */
    private String bizType;
    /**
     * 业务单据ID（wms_order.id 或 wms_inventory_adjust.id）
     */
    private Long bizId;
    /**
     * 业务单据编号，冗余方便查询
     */
    private Long bizNo;
    /**
     * 本次变动前的库存量
     */
    private BigDecimal beforeQuantity;
    /**
     * 本次变动量。入库为正、出库为负
     */
    private BigDecimal changeQuantity;
    /**
     * 本次变动后的库存量
     */
    private BigDecimal afterQuantity;


}
