package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.ip.core.Area;
import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import vip.appap.suxin.module.system.enums.SexEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 合作伙伴 DO（统一用户表）
 *
 * 字段归属说明：
 * - 本表存储通用用户数据（账号信息、基本业务标记、会员信息、CRM 信息）
 * - 婚恋专属数据（择偶条件、身高体重等）存储在 partner_marriage 表（1:1 关联）
 * - 租户隔离: 本表通过 TenantBaseDO 自动注入 tenant_id，MyBatis-Plus 拦截器自动过滤
 *
 * @see vip.appap.suxin.module.marriage.dal.dataobject.PartnerMarriageProfileDO 婚恋档案扩展数据
 */
@TableName(value = "partner", autoResultMap = true)
@KeySequence("partner_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDO extends TenantBaseDO {

    // ========== 账号信息 ==========

    /**
     * 用户ID
     */
    @TableId
    private Long id;
    /**
     * 手机
     */
    private String mobile;
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
     * 所属行业
     */
    private Integer industryId;
    /**
     * 帐号状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // ========== 基础信息 ==========

    /**
     * 客户/伙伴名称
     */
    private String name;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 性别
     *
     * 枚举 {@link SexEnum}
     */
    private Integer sex;
    /**
     * 用户邮箱
     */
    private String email;
    /**
     * 出生日期
     */
    private LocalDateTime birthday;
    /**
     * 所在地
     *
     * 关联 {@link Area#getId()} 字段
     */
    private Integer areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 备注
     */
    private String remark;

    // ========== 业务角色 ==========

    /**
     * 是否客户
     */
    private Boolean isCustomer;
    /**
     * 是否供应商
     */
    private Boolean isSupplier;
    /**
     * 供应商等级
     *
     * 关联 partner_level.id（type='supplier'）
     */
    private Long supplierLevel;
    /**
     * 供应商评分
     */
    private Integer supplierScore;
    /**
     * 是否公司
     */
    private Boolean isCompany;
    /**
     * 是否会员
     */
    private Boolean isMember;
    /**
     * 会员到期时间
     */
    private LocalDateTime memberExpireTime;

    // ========== 会员专属信息（原 member 表字段，已融合到 partner 表） ==========

    /**
     * 积分
     */
    private Integer point;
    /**
     * 等级ID
     *
     * 关联 partner_level.id
     */
    private Long customerLevel;
    /**
     * 经验值
     */
    private Integer experience;
    /**
     * 分组ID
     *
     * 关联 partner_group.id
     */
    private Long groupId;
    /**
     * 标签ID列表（逗号分隔）
     */
    private String tagIds;
    /**
     * 注册IP
     */
    private String registerIp;
    /**
     * 注册终端
     */
    private Integer registerTerminal;

    // ==================== CRM 客户业务字段（原 partner_sales） ====================

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
     * 成为负责人的时间
     */
    private LocalDateTime ownerTime;
    /**
     * 锁定状态
     */
    private Boolean lockStatus;
    /**
     * 成交状态
     */
    private Boolean dealStatus;
    /**
     * 客户等级（字典方式）
     *
     * 与 {@link #customerLevel}（关联 partner_level 表，type='customer'）不同，这是 CRM 字典 CRM_CUSTOMER_LEVEL
     */
    private Integer salesLevel;
    /**
     * 客户来源（字典方式）
     *
     * 字典 CRM_CUSTOMER_SOURCE
     */
    private Integer salesSource;

}
