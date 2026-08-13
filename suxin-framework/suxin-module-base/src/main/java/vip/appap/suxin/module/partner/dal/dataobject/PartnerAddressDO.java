package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员地址 DO
 *
 * @author 书心软件
 */
@TableName("partner_address")
@KeySequence("partner_address_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerAddressDO extends BaseDO {

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
     * 是否默认
     */
    @TableField("default_status")
    private Boolean defaulted;
    /**
     * 地址类型
     *
     * 0 - 会员收件地址
     * 1 - 商户寄件地址（租户内共享，供电子面单等运营能力选择）
     *
     * 枚举 {@link vip.appap.suxin.module.partner.enums.PartnerAddressTypeEnum}
     */
    private Integer type;
    /**
     * 收件人名称
     */
    private String name;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 地区编号
     */
    private Integer areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 邮编
     */
    private String postCode;

}
