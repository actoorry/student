package vip.appap.suxin.module.wms.dal.dataobject.inventoryadjust;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 盘点调整记录 DO
 *
 * @author admin
 */
@TableName("wms_inventory_adjust")
@KeySequence("wms_inventory_adjust_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsInventoryAdjustDO extends BaseDO {

    /** 主键 */
    @TableId
    private Long id;
    /** 单据编号，Redis INCR 生成 */
    private Long no;
    /** 盘点仓库，FK -> wms_warehouse.id（实体仓库） */
    private Long warehouseId;
    /** 产品SKU ID，FK -> product_sku.id */
    private Long skuId;
    /** 库位ID，FK -> wms_warehouse.id */
    private Long locationId;
    /** 批次号 */
    private String batchNo;
    /** 生产日期 */
    private LocalDate productionDate;
    /** 有效期至 */
    private LocalDate expiryDate;
    /** 账面数量，系统从wms_stock读取的当前库存 */
    private BigDecimal bookQuantity;
    /** 实盘数量，用户实际盘点后填入 */
    private BigDecimal actualQuantity;
    /** 状态：0=草稿 1=已审核 */
    private Integer status;
    /** 盘点类型：1=全盘 2=抽盘 3=手动调整 */
    private Integer checkType;
    /** 盘点时间 */
    private LocalDateTime checkTime;
    /** 操作人ID */
    private Long operatorId;
    /** 备注 */
    private String remark;
}