package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 交易订单创建 Request VO")
@Data
public class AppSalesOrderCreateReqVO extends AppSalesOrderSettlementReqVO {

    @Schema(description = "备注", example = "这个是我的订单哟")
    private String remark;

}
