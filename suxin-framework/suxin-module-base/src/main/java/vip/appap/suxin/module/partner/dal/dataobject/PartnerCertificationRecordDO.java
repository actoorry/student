package vip.appap.suxin.module.partner.dal.dataobject;

import vip.appap.suxin.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 合作伙伴认证记录 DO
 */
@TableName("partner_certification_record")
@KeySequence("partner_certification_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCertificationRecordDO extends TenantBaseDO {

    @TableId
    private Long id;

    /**
     * 合作伙伴ID
     */
    private Long partnerId;

    /**
     * 认证类型，字典 partner_cert_type
     */
    private String certType;

    /**
     * 请求去重KEY
     */
    private String requestKey;

    /**
     * 供应商编码，字典 partner_cert_provider
     */
    private String providerCode;

    /**
     * 供应商流水号
     */
    private String providerSeqNo;

    /**
     * 供应商返回 code
     */
    private String code;

    /**
     * 供应商返回 message
     */
    private String message;

    /**
     * 认证结果，字典 partner_cert_state
     */
    private String state;

    /**
     * 是否计费
     */
    private Boolean charged;

    /**
     * 请求参数快照
     */
    private String requestParams;

    /**
     * 供应商完整响应
     */
    private String responseBody;

}
