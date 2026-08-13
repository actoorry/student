package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.module.sales.enums.SalesProductScopeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 优惠劵模板分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSalesCouponTemplatePageReqVO extends PageParam {

    @Schema(description = "商品范围", example = "1")
    @InEnum(value = SalesProductScopeEnum.class, message = "商品范围，必须是 {value}")
    private Integer productScope;

    @Schema(description = "商品标号", example = "1")
    private Long spuId;

}
