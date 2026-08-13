package vip.appap.suxin.module.hr.controller.admin.certificate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 证书督查台账汇总 Response VO")
@Data
public class CertificateLedgerSummaryRespVO {

    @Schema(description = "已过期数量")
    private Long expiredCount;

    @Schema(description = "30 天内即将到期数量（累计口径）")
    private Long expiringCount;

    @Schema(description = "60 天内即将到期数量（累计口径，含 30 天）")
    private Long expiring60Count;

    @Schema(description = "90 天内即将到期数量（累计口径，含 30/60 天）")
    private Long expiring90Count;

    @Schema(description = "考核逾期数量")
    private Long assessmentOverdueCount;

    @Schema(description = "考核前 60 天数量（下次考核日在今天至今天+60）")
    private Long assessmentDue60Count;

}
