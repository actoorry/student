package vip.appap.suxin.module.sales.controller.admin.vo;


import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillProductBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 秒杀活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesSeckillActivityCreateReqVO extends SalesSeckillActivityBaseVO {

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SalesSeckillProductBaseVO> products;

}
