package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员积分记录 DO
 *
 * @author 书心软件
 */
@TableName("partner_point_record")
@KeySequence("partner_point_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerPointRecordDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 业务编码
     */
    private String bizId;
    /**
     * 业务类型
     */
    private Integer bizType;
    /**
     * 积分标题
     */
    private String title;
    /**
     * 积分描述
     */
    private String description;
    /**
     * 积分
     */
    private Integer point;
    /**
     * 变动后的积分
     */
    private Integer totalPoint;

}
