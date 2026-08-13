package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 快递运费模板包邮 Base VO，提供给添加运费模板使用
 */
@Data
public class SalesDeliveryExpressTemplateFreeBaseVO {

    @Schema(description = "编号", example = "6592", hidden = true) // 更新时复用，保留子规则 id 支持 diff
    private Long id;

    @Schema(description = "区域编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,120000]")
    @NotEmpty(message = "区域编号列表不能为空")
    private List<Integer> areaIds;

    @Schema(description = "包邮金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @NotNull(message = "包邮金额不能为空")
    @Min(value = 1, message = "包邮金额必须大于 0")
    private Integer freePrice;

    @Schema(description = "包邮件数", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "包邮件数不能为空")
    @Min(value = 1, message = "包邮件数必须大于 0")
    private Integer freeCount;

}
