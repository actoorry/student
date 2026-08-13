package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.module.partner.enums.MemberTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 会员赠送 Request VO")
@Data
public class PartnerMemberGrantReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    @Schema(description = "会员类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "会员类型不能为空")
    @InEnum(MemberTypeEnum.class)
    private Integer memberType;

    @Schema(description = "会员时长（月）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    @NotNull(message = "会员时长不能为空")
    @Min(value = 1, message = "会员时长必须大于 0")
    private BigDecimal durationQuantity;

    @Schema(description = "会员时长单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "102")
    @NotNull(message = "会员时长单位不能为空")
    private Long durationUnitId;

    @Schema(description = "备注", example = "运营赠送")
    @NotBlank(message = "备注不能为空")
    private String remark;

}
