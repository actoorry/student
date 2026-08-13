package vip.appap.suxin.module.partner.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;

/**
 * 销售购物车 DO。
 *
 * 每个用户和 SKU 对应一条活动记录，通过 {@link #spuId} 和 {@link #skuId} 关联商品主数据。
 */
@TableName("sales_cart")
@KeySequence("sales_cart_seq")
@Data
public class SalesCartDO extends BaseDO {

    private Long id;
    private Long userId;
    private Long spuId;
    private Long skuId;
    private Integer count;
    private Boolean selected;

}
