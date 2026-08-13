package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesSeckillProductRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Schema(description = "管理后台 - 秒杀活动的详细 Response VO")
@Data
@ToString(callSuper = true)
public class SalesSeckillActivityDetailRespVO extends SalesSeckillActivityBaseVO{

    @Schema(description = "秒杀活动id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "秒杀商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SalesSeckillProductRespVO> products;

}
