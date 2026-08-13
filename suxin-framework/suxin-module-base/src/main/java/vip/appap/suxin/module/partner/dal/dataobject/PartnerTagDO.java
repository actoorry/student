package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员标签 DO
 *
 * @author 书心软件
 */
@TableName("partner_tag")
@KeySequence("partner_tag_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerTagDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 标签名称
     */
    private String name;

}
