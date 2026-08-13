package vip.appap.suxin.module.wms.dal.dataobject.order;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import vip.appap.suxin.module.system.dal.dataobject.TenantDO;

/**
 * 出入库/调拨单据头 DO
 *
 * @author admin
 */
@TableName("wms_order")
@KeySequence("wms_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOrderDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 单据编号，Redis INCR 生成
     */
    private Long no;
    /**
     * 单据类型，FK -> wms_order_type.id
     */
    private Long typeId;
    /**
     * 状态：0=草稿 1=待审核 2=已审核 3=已完成 -1=已取消
     */
    private Integer status;
    /**
     * 供应商ID，FK -> partner.id。need_supplier=1时必填
     */
    private Long supplierId;
    /**
     * 客户ID，FK -> partner.id。need_customer=1时必填
     */
    private Long customerId;
    /**
     * 来源位置。实体库位或虚拟仓ID
     */
    private Long fromWarehouseId;
    /**
     * 去向位置。实体库位或虚拟仓ID
     */
    private Long toWarehouseId;
    /**
     * 源单据ID（如来自采购单或销售单）
     */
    private Long originId;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 单据日期
     */
    private LocalDateTime orderTime;
    /**
     * 备注
     */
    private String remark;


}
