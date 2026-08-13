package vip.appap.suxin.module.marriage.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 婚恋档案扩展表 DO
 *
 * 字段归属说明：
 * - 本表仅存储婚恋专属数据（个人条件、择偶条件、认证状态、档案状态）
 * - 通用用户数据（昵称、头像、手机号等）存储在 partner 表
 * - 关联关系: partner_marriage.id = partner.id（1:1）
 * - 租户隔离: 本表有独立的 tenant_id 字段
 *
 * @see vip.appap.suxin.module.partner.dal.dataobject.PartnerDO 通用用户数据
 */
@TableName(value = "partner_marriage", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerMarriageProfileDO {

    @TableId
    private Long id;                   // = partner.id

    // ===== 个人条件 =====
    private Integer maritalStatus;     // 婚姻状态
    private Integer heightCm;          // 身高(cm)
    private Integer weightKg;          // 体重(kg)
    private Integer education;         // 学历
    private Integer incomeLevel;       // 收入档位
    private Long liveAreaId;           // 现居地区ID
    private Integer houseStatus;       // 房产情况
    private Integer carStatus;         // 车辆情况
    private String jobTitle;           // 职业/职位
    private String bio;                // 自我介绍

    // ===== 择偶条件 =====
    private Integer mateMaritalStatus;    // 择偶婚姻状态
    private Integer mateMinHeightCm;      // 择偶最小身高
    private Integer mateMaxHeightCm;      // 择偶最大身高
    private Integer mateMinWeightKg;      // 择偶最小体重
    private Integer mateMaxWeightKg;      // 择偶最大体重
    private Integer mateMinEducation;     // 择偶最低学历
    private Long mateLiveAreaId;          // 择偶现居地区ID
    private Integer mateMinIncomeLevel;   // 择偶最低收入档位
    private Integer mateHouseStatus;      // 择偶房产情况
    private Integer mateCarStatus;        // 择偶车辆情况
    private String mateJobTitle;          // 择偶职业/职位
    private String mateRemark;            // 择偶条件补充说明

    // ===== 展示信息 =====
    private String backgroundImage;   // 背景图
    // ===== 认证状态 =====
    private Integer realVerified;     // 是否实名 (0=未实名, 1=已实名)

    // ===== 档案状态 =====
    private Integer profileStatus;    // 档案状态
    private Integer recommendSort;    // 首页推荐排序
    private Boolean recommendFlag;    // 是否首页推荐
    private LocalDateTime onShelfTime; // 上架时间
    private String offShelfReason;    // 下架原因

    private Long tenantId;            // 租户编号

}
