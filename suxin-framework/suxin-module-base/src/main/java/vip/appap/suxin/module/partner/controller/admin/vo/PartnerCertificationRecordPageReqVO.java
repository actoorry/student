package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 合作伙伴认证记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerCertificationRecordPageReqVO extends PageParam {

    @Schema(description = "合作伙伴ID", example = "1024")
    private Long partnerId;

    @Schema(description = "认证类型，字典 partner_cert_type", example = "REAL_NAME")
    private String certType;

    @Schema(description = "供应商编码，字典 partner_cert_provider", example = "chinadatapay")
    private String providerCode;

    @Schema(description = "认证结果，字典 partner_cert_state", example = "1")
    private String state;

    @Schema(description = "供应商流水号", example = "FPH8D10D260605142553911")
    private String providerSeqNo;

    @Schema(description = "合作伙伴名称（模糊匹配）", example = "张三")
    private String partnerName;

    @Schema(description = "合作伙伴手机号（精确匹配）", example = "13800138000")
    private String partnerMobile;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
