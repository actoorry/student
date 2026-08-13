package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.excel.core.annotations.DictFormat;
import vip.appap.suxin.module.partner.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 合作伙伴认证记录 Response VO")
@Data
public class PartnerCertificationRecordRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "合作伙伴ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long partnerId;

    @Schema(description = "认证类型，字典 partner_cert_type", requiredMode = Schema.RequiredMode.REQUIRED, example = "REAL_NAME")
    @DictFormat(DictTypeConstants.PARTNER_CERT_TYPE)
    private String certType;

    @Schema(description = "请求去重KEY", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestKey;

    @Schema(description = "供应商编码，字典 partner_cert_provider", requiredMode = Schema.RequiredMode.REQUIRED, example = "chinadatapay")
    @DictFormat(DictTypeConstants.PARTNER_CERT_PROVIDER)
    private String providerCode;

    @Schema(description = "供应商流水号", example = "FPH8D10D260605142553911")
    private String providerSeqNo;

    @Schema(description = "供应商返回 code", example = "10000")
    private String code;

    @Schema(description = "供应商返回 message", example = "成功")
    private String message;

    @Schema(description = "认证结果，字典 partner_cert_state", example = "1")
    @DictFormat(DictTypeConstants.PARTNER_CERT_STATE)
    private String state;

    @Schema(description = "是否计费")
    private Boolean charged;

    @Schema(description = "请求参数快照")
    private String requestParams;

    @Schema(description = "供应商完整响应")
    private String responseBody;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
