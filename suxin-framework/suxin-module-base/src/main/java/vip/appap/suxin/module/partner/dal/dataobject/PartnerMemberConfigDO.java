package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import vip.appap.suxin.module.partner.enums.MemberTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("partner_member_config")
@KeySequence("partner_member_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerMemberConfigDO extends TenantBaseDO {

    public static final String SCENE_MARRIAGE = "marriage";

    @TableId
    private Long id;

    /**
     * 业务场景，例如 marriage。
     */
    private String scene;

    /**
     * 枚举 {@link MemberTypeEnum}
     */
    private Integer memberType;

    /**
     * 会员套餐商品分类编号。
     */
    private Long memberCategoryId;

    /**
     * 支付应用标识，例如 mall。
     */
    private String payAppKey;

    /**
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
