package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Collection;

@Schema(description = "用户 App - 购物车更新是否选中 Request VO")
@Data
public class AppSalesCartUpdateSelectedReqVO {

    @Schema(description = "购物项编号列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024,2048")
    @NotEmpty(message = "编号列表不能为空")
    private Collection<Long> ids;

    @Schema(description = "是否选中", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否选中不能为空")
    private Boolean selected;

}
