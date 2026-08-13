package vip.appap.suxin.module.accountant.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 修改钱包余额 Request VO")
@Data
public class AccountUpdateBalanceReqVO {

    @Schema(description = "客商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    @NotNull(message = "客商编号不能为空")
    private Long partnerId;

    @Schema(description = "变动余额，正数为增加，负数为减少", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "变动余额不能为空")
    private Integer balance;

}

