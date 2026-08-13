package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.validation.InEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
* 快递公司 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SalesDeliveryExpressBaseVO {

    @Schema(description = "快递公司编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "快递公司编码不能为空")
    @Size(max = 64, message = "快递公司编码长度不能超过 64")
    private String code;

    @Schema(description = "快递公司名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotBlank(message = "快递公司名称不能为空")
    @Size(max = 64, message = "快递公司名称长度不能超过 64")
    private String name;

    @Schema(description = "快递公司logo")
    @Size(max = 256, message = "快递公司 logo 长度不能超过 256")
    private String logo;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序必须大于等于 0")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
