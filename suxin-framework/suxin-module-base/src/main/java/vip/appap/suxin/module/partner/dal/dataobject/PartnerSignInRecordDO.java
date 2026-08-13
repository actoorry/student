package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员签到记录 DO
 *
 * @author 书心软件
 */
@TableName("partner_sign_in_record")
@KeySequence("partner_sign_in_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSignInRecordDO extends BaseDO {

    /**
     * 签到自增id
     */
    @TableId
    private Long id;
    /**
     * 签到用户
     */
    private Long userId;
    /**
     * 第几天签到
     */
    private Integer day;
    /**
     * 签到的分数
     */
    private Integer point;
    /**
     * 奖励经验
     */
    private Integer experience;

}
