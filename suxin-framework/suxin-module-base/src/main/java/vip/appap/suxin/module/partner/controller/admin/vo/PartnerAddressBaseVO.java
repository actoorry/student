package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 会员地址 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PartnerAddressBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "是否默认", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否默认不能为空")
    private Boolean defaulted;

    @Schema(description = "地址类型：0-会员收件地址 1-商户寄件地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "地址类型不能为空")
    private Integer type;

    @Schema(description = "收件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotNull(message = "收件人名称不能为空")
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    @NotNull(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4371")
    @NotNull(message = "地区编号不能为空")
    private Integer areaId;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "XX路XX号")
    @NotNull(message = "详细地址不能为空")
    private String detailAddress;

    @Schema(description = "邮编", example = "200000")
    private String postCode;

}
