package vip.appap.suxin.module.crm.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import vip.appap.suxin.module.partner.dal.dataobject.PartnerDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CRM 线索 DO
 *
 * 基础信息（name, mobile, email 等）存储在 partner 表，通过 id 共享主键关联
 *
 * @author Wanwan
 */
@TableName("crm_clue")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmClueDO extends BaseDO {

    /**
     * 编号，等于 partner.id（共享主键）
     */
    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 线索名称
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
     * 跟进状态
     */
    private Boolean followUpStatus;
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
     * 负责人的用户编号
     *
     * 关联 AdminUserDO 的 id 字段
     */
    private Long ownerUserId;
    /**
     * 线索等级
     *
     * 字典 CRM_CUSTOMER_LEVEL
     */
    private Integer level;
    /**
     * 线索来源
     *
     * 字典 CRM_CUSTOMER_SOURCE
     */
    private Integer source;

    /**
     * 转化状态
     *
     * true 表示已转换，会更新 {@link #customerId} 字段
     */
    private Boolean transformStatus;
    /**
     * 客户编号
     *
     * 关联 {@link PartnerDO#getId()}
     */
    private Long customerId;

}
