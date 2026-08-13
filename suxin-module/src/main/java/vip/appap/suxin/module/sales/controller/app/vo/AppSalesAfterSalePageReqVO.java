package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Schema(description = "用户 App - 交易售后分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSalesAfterSalePageReqVO extends PageParam {

    @Schema(description = "售后状态", example = "10, 20")
    private Set<Integer> statuses;

}
