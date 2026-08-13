package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 装修模板更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesDiyTemplateUpdateReqVO extends SalesDiyTemplateBaseVO {

    @Schema(description = "装修模板编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    @NotNull(message = "装修模板编号不能为空")
    private Long id;

}
