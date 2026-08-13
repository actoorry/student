package vip.appap.suxin.module.accountant.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 用户钱包明细 Request VO")
@Data
public class AccountUserReqVO {

    @Schema(description = "客商编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "客商编号不能为空")
    private Long partnerId;

}

