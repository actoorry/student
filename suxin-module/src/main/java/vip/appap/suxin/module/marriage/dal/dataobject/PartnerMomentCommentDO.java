package vip.appap.suxin.module.marriage.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@TableName("partner_moment_comment")
@KeySequence("partner_moment_comment_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerMomentCommentDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long momentId;
    private Long partnerId;
    private Long parentId;
    private Long replyToPartnerId;
    private String content;
    private Integer status;
    private Integer likeCount;

}
