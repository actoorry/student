package vip.appap.suxin.module.product.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 产品单位 DO
 *
 * @author 书心软件
 */
@TableName("product_unit")
@KeySequence("product_unit_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitDO extends BaseDO {

    /**
     * 单位编号
     */
    @TableId
    private Long id;
    /**
     * 单位名称
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 单位类型
     *
     * 字典 {@link vip.appap.suxin.module.product.enums.DictTypeConstants#PRODUCT_UNIT_TYPE}
     */
    private Integer type;
    /**
     * 换算到同类型基础单位的数量，可为 1。
     */
    private String relativeFactor;
    /**
     * 兼容保留字段，当前单位管理不再使用该字段做换算关系。
     */
    private Long relativeUnitId;

}
