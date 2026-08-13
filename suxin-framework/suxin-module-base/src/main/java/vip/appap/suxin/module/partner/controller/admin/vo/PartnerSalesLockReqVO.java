package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - CRM 客户锁定/解锁 Request VO")
@Data
public class PartnerSalesLockReqVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13563")
    private Long id;

    @Schema(description = "客户锁定状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Boolean lockStatus;

}
