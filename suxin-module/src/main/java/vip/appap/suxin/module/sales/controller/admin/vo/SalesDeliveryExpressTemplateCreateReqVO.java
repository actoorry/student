package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 快递运费模板创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesDeliveryExpressTemplateCreateReqVO extends SalesDeliveryExpressTemplateBaseVO {

    @Schema(description = "区域运费列表")
    @Valid
    @NotEmpty(message = "至少需要一条计费规则")
    private List<SalesDeliveryExpressTemplateChargeBaseVO> charges;

    @Schema(description = "包邮区域列表")
    @Valid
    private List<SalesDeliveryExpressTemplateFreeBaseVO> frees;

}
