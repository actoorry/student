package vip.appap.suxin.module.accountant.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 用户钱包 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class AccountBaseVO {

    @Schema(description = "客商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20020")
    @NotNull(message = "客商编号不能为空")
    private Long partnerId;

    @Schema(description = "余额，单位分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "余额，单位分不能为空")
    private Integer balance;

    @Schema(description = "累计支出，单位分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "累计支出，单位分不能为空")
    private Integer totalExpense;

    @Schema(description = "累计充值，单位分", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "累计充值，单位分不能为空")
    private Integer totalRecharge;

    @Schema(description = "冻结金额，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "20737")
    @NotNull(message = "冻结金额，单位分不能为空")
    private Integer freezePrice;

    @Schema(description = "余额类型", example = "RECHARGE")
    private String balanceType;

}
