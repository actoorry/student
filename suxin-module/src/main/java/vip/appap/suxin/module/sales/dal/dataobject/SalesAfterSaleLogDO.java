package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.common.enums.UserTypeEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleOperateTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 交易售后日志 DO
 *
 * @author 书心软件
 */
@TableName("sales_after_sale_log")
@KeySequence("sales_after_sale_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesAfterSaleLogDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 1：AdminUserDO 的 id 字段
     * 关联 2：MemberUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 售后编号
     *
     * 关联 {@link SalesAfterSaleDO#getId()}
     */
    private Long afterSaleId;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    private Integer afterStatus;

    /**
     * 操作类型
     *
     * 枚举 {@link SalesAfterSaleOperateTypeEnum}
     */
    private Integer operateType;
    /**
     * 操作明细
     */
    private String content;

}
