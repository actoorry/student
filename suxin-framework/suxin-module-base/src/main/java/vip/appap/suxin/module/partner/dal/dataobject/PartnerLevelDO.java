package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员等级 DO
 *
 * @author 书心软件
 */
@TableName("partner_level")
@KeySequence("partner_level_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLevelDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 等级名称
     */
    private String name;
    /**
     * 等级
     */
    private Integer level;
    /**
     * 升级经验
     */
    private Integer experience;
    /**
     * 享受折扣
     */
    private Integer discountPercent;
    /**
     * 等级图标
     */
    private String icon;
    /**
     * 等级背景图
     */
    private String backgroundUrl;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
