package vip.appap.suxin.module.accountant.controller.app.vo;

import vip.appap.suxin.module.accountant.controller.admin.vo.PayOrderSubmitRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 APP - 支付订单提交 Response VO")
@Data
public class AppPayOrderSubmitRespVO extends PayOrderSubmitRespVO {

}

