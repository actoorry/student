package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.module.sales.enums.SalesCombinationRecordStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 拼团记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSalesCombinationRecordPageReqVO extends PageParam {

    @Schema(description = "拼团状态", example = "1")
    @InEnum(value = SalesCombinationRecordStatusEnum.class, message = "拼团状态必须是 {value}")
    private Integer status;

}
