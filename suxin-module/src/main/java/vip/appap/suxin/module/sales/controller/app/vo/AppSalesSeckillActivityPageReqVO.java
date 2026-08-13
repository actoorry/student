package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 商品评价分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppSalesSeckillActivityPageReqVO extends PageParam {

    @Schema(description = "秒杀配置编号", example = "1024")
    private Long configId;

}
