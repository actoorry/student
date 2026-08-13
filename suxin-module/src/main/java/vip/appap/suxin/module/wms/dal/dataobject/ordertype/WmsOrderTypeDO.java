package vip.appap.suxin.module.wms.dal.dataobject.ordertype;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 出入库单据类型配置 DO
 *
 * @author admin
 */
@TableName("wms_order_type")
@KeySequence("wms_order_type_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsOrderTypeDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 单据类型名称，如"采购入库"
     */
    private String name;
    /**
     * 默认来源位置。0=用户自选实体位置，1-6=固定虚拟仓
     */
    private Long fromLocationId;
    /**
     * 默认去向位置。0=用户自选，1-6=固定虚拟仓
     */
    private Long toLocationId;
    /**
     * 库存影响方向：1=入库（增库存） 2=出库（减库存）
     */
    private Integer stockImpact;
    /**
     * 是否强制填写供应商：0=否 1=是
     */
    private Integer needSupplier;
    /**
     * 是否强制填写客户：0=否 1=是
     */
    private Integer needCustomer;
    /**
     * 是否启用：0=停用 1=启用
     */
    private Integer active;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;


}