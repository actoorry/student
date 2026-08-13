package vip.appap.suxin.module.sales.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.sales.enums.SalesDeliveryExpressChargeModeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 快递运费模板 DO
 *
 * @author jason
 */
@TableName("sales_delivery_express_template")
@KeySequence("sales_delivery_express_template_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class SalesDeliveryExpressTemplateDO extends BaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 配送计费方式
     *
     * 枚举 {@link SalesDeliveryExpressChargeModeEnum}
     */
    private Integer chargeMode;

    /**
     * 排序
     */
    private Integer sort;

}
