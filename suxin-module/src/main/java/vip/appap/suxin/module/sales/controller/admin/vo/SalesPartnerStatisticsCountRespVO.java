package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 会员数量统计 Response VO")
@Data
public class SalesPartnerStatisticsCountRespVO {

    @Schema(description = "用户访问量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer visitUserCount;

    @Schema(description = "注册用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer registerUserCount;

}
