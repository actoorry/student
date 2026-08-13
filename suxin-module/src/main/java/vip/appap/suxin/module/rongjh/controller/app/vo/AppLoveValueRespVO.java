package vip.appap.suxin.module.rongjh.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "用户 APP - 平台爱心贡献值 Response VO")
@Data
public class AppLoveValueRespVO {

    @Schema(description = "累计爱心值")
    private BigDecimal totalLoveValues;

}
