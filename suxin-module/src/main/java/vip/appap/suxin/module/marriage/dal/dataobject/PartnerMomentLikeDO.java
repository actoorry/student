package vip.appap.suxin.module.marriage.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("partner_moment_like")
@KeySequence("partner_moment_like_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerMomentLikeDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long momentId;
    private Long partnerId;

}
