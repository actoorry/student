package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.module.sales.enums.SalesCouponStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 优惠劵分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSalesCouponPageReqVO extends PageParam {

    @Schema(description = "优惠劵状态", example = "1")
    @InEnum(value = SalesCouponStatusEnum.class, message = "优惠劵状态，必须是 {value}")
    private Integer status;

}
