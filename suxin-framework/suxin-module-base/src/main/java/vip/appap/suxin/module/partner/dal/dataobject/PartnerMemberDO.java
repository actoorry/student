package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.partner.enums.MemberSourceTypeEnum;
import vip.appap.suxin.module.partner.enums.MemberStatusEnum;
import vip.appap.suxin.module.partner.enums.MemberTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("partner_member")
@KeySequence("partner_member_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerMemberDO extends BaseDO {

    @TableId
    private Long id;

    private Long userId;

    /**
     * 枚举 {@link MemberTypeEnum}
     */
    private Integer memberType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /**
     * 枚举 {@link MemberStatusEnum}
     */
    private Integer status;

    /**
     * 枚举 {@link MemberSourceTypeEnum}
     */
    private Integer sourceType;

    private Long orderId;

    private Long orderItemId;

    private Long spuId;

    private Long skuId;

    private BigDecimal durationQuantity;

    private Long durationUnitId;

    private String remark;

}
