package vip.appap.suxin.module.partner.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外部认证接口调用结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCertificationCallResult {

    private String code;

    private String message;

    private String state;

    private String providerSeqNo;

    private String responseBody;

}
