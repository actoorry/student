package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@TableName(value = "partner_rel_partner", autoResultMap = true)
@KeySequence("partner_rel_partner_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRelPartnerDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * Relation owner partner id.
     */
    private Long partnerId;

    /**
     * Related partner id.
     */
    private Long relPartnerId;

    /**
     * Relation type, such as attention.
     */
    private String type;

}
