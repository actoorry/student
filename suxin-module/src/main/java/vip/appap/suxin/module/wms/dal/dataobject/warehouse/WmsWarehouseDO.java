package vip.appap.suxin.module.wms.dal.dataobject.warehouse;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 仓库位置表（仓库/库区/库位树形结构） DO
 *
 * @author admin
 */
@TableName("wms_warehouse")
@KeySequence("wms_warehouse_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsWarehouseDO extends BaseDO {

    /**
     * 主键（1-6保留给虚拟仓，用户数据从100起）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 上级节点。0=实体仓库根节点；等于自身id=虚拟仓（不可移动）
     */
    private Long parentId;
    /**
     * 名称，如"原料仓"、"A区"、"A-01"
     */
    private String name;
    /**
     * 编码，非必填，无唯一约束，仅用于展示或外部系统对接
     */
    private String code;
    /**
     * 归属城市区县ID，FK -> Area.getId()，值为Area主键ID，通过AreaUtils.format(areaId)获取名称
     */
    private Integer areaId;
    /**
     * 详细地址，实体仓库层级填写
     */
    private String address;
    /**
     * 仓库负责人ID，FK -> partner.id；仅允许类型为【公司】的客商；仅一级实体仓库可配置
     */
    private Long partnerId;
    /**
     * 状态：0=禁用 1=启用
     */
    private Integer status;
    /**
     * 同级排序，越小越前
     */
    private Integer sort;
    /**
     * 位置类型：0=view 虚拟节点 1=warehouse 实体仓库 2=area 库区 3=location 库位
     * 4=supplier_virtual 供应商虚拟 5=customer_virtual 客户虚拟 6=inventory_virtual 盘点差异
     * 7=scrap_virtual 报废 8=production_virtual 生产 9=transit_virtual 在途
     */
    private Integer locType;
    /**
     * 备注
     */
    private String remark;


}
