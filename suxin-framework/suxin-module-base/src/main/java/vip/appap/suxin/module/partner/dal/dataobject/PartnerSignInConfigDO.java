package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员签到配置 DO
 *
 * @author 书心软件
 */
@TableName("partner_sign_in_config")
@KeySequence("partner_sign_in_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSignInConfigDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Integer id;
    /**
     * 第几天
     */
    private Integer day;
    /**
     * 奖励积分
     */
    private Integer point;
    /**
     * 奖励经验
     */
    private Integer experience;
    /**
     * 状态
     */
    private Integer status;

}
