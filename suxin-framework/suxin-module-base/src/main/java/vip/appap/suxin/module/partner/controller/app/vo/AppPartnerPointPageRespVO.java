package vip.appap.suxin.module.partner.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 积分页 Response VO")
@Data
public class AppPartnerPointPageRespVO {

    @Schema(description = "当前积分余额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalPoint;

    @Schema(description = "积分记录分页", requiredMode = Schema.RequiredMode.REQUIRED)
    private PageResult<AppPartnerPointRecordRespVO> records;

}
