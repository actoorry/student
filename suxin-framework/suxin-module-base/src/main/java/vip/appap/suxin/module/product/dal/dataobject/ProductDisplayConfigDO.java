package vip.appap.suxin.module.product.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品展示配置 DO
 */
@TableName("product_display_config")
@KeySequence("product_display_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayConfigDO extends TenantBaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 展示场景编码
     */
    private String sceneCode;

    /**
     * 展示场景名称
     */
    private String sceneName;

    /**
     * 商品销售分类 ID 集合，英文逗号分隔
     */
    private String categoryIds;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

}
