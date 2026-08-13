package vip.appap.suxin.module.rongjh.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 战友会审核 Request VO")
@Data
public class WarriorAuditReqVO {

    @Schema(description = "会员编号(partner.id)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "审核备注")
    private String remark;

}
