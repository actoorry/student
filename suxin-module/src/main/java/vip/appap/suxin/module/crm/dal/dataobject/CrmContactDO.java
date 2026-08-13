package vip.appap.suxin.module.crm.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CRM 联系人 DO
 *
 * 基础信息（name, mobile, email 等）存储在 partner 表，通过 id 共享主键关联
 *
 * @author 书心软件
 */
@TableName("crm_contact")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmContactDO extends BaseDO {

    /**
     * 主键，等于 partner.id（共享主键）
     */
    @TableId(type = IdType.INPUT)
    private Long id;
    /**
     * 联系人名称
     */
    private String name;
    /**
     * 固定电话
     */
    private String telephone;
    /**
     * QQ
     */
    private String qq;
    /**
     * 微信
     */
    private String wechat;

    /**
     * 客户编号
     *
     * 关联 {@link PartnerDO#getId()}
     */
    private Long customerId;

    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;
    /**
     * 最后跟进内容
     */
    private String contactLastContent;
    /**
     * 下次联系时间
     */
    private LocalDateTime contactNextTime;

    /**
     * 负责人用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;

    /**
     * 是否关键决策人
     */
    private Boolean master;
    /**
     * 直属上级
     *
     * 关联 {@link CrmContactDO#id}
     */
    private Long parentId;

}
