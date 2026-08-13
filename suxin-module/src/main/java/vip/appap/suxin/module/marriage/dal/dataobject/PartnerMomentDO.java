package vip.appap.suxin.module.marriage.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@TableName("partner_moment")
@KeySequence("partner_moment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerMomentDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partnerId;
    private String content;
    private Integer mediaType;
    private Integer mediaCount;
    private Integer visibility;
    private Integer status;
    private String auditReason;
    private Integer pvCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Boolean recommendFlag;
    private Integer recommendSort;
    private LocalDateTime publishTime;

}
