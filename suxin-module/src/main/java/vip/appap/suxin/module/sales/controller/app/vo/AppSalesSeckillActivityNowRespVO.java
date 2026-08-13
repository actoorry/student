package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.module.sales.controller.app.vo.AppSalesSeckillConfigRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 当前秒杀活动 Response VO")
@Data
public class AppSalesSeckillActivityNowRespVO {

    @Schema(description = "秒杀时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private AppSalesSeckillConfigRespVO config;

    @Schema(description = "秒杀活动数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AppSalesSeckillActivityRespVO> activities;

}
