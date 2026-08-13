package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 合作伙伴认证记录（含合作伙伴信息） Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerCertificationRecordWithPartnerRespVO extends PartnerCertificationRecordRespVO {

    @Schema(description = "合作伙伴名称", example = "张三")
    private String partnerName;

    @Schema(description = "合作伙伴手机号", example = "13800138000")
    private String partnerMobile;

    @Schema(description = "认证状态标签", example = "通过")
    private String stateLabel;

}
