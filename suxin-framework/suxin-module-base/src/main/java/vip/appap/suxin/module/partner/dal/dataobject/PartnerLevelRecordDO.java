package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员等级记录 DO
 *
 * @author 书心软件
 */
@TableName("partner_level_record")
@KeySequence("partner_level_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLevelRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 等级编号
     */
    private Long levelId;
    /**
     * 会员等级
     */
    private Integer level;
    /**
     * 享受折扣
     */
    private Integer discountPercent;
    /**
     * 升级经验
     */
    private Integer experience;
    /**
     * 会员此时的经验
     */
    private Integer userExperience;
    /**
     * 备注
     */
    private String remark;
    /**
     * 描述
     */
    private String description;

}
