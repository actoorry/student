package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 分销用户 - 清除推广员 Request VO")
@Data
@ToString(callSuper = true)
public class SalesBrokerageUserClearBrokerageUserReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20019")
    @NotNull(message = "用户编号不能为空")
    private Long id;

}
