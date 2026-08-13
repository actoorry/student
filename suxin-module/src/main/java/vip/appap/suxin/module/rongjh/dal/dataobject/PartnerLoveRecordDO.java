package vip.appap.suxin.module.rongjh.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@TableName("partner_love_record")
@KeySequence("partner_love_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLoveRecordDO extends BaseDO {

    @TableId
    private Long id;

    private Long userId;
    private Long orderId;
    private Long orderItemId;
    private String bizType;
    private String bizKey;
    private Long amountFen;
    private BigDecimal loveValue;
    private String remark;
}
