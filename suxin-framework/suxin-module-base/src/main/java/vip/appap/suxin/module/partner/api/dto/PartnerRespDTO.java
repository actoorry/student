package vip.appap.suxin.module.partner.api.dto;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合作伙伴用户信息 Response DTO
 */
@Data
public class PartnerRespDTO {

    /**
     * 用户编号
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 账号状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 是否客户
     */
    private Boolean isCustomer;
    /**
     * 是否供应商
     */
    private Boolean isSupplier;
    /**
     * 是否公司
     */
    private Boolean isCompany;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 用户积分
     */
    private Integer point;

    /**
     * 用户等级编号
     */
    private Long customerLevel;

    /**
     * 创建时间（注册时间）
     */
    private LocalDateTime createTime;

}
