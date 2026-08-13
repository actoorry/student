package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationActivityRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 拼团记录的分页项 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesCombinationRecordPageItemRespVO extends SalesCombinationRecordBaseVO {

    // ========== 活动相关 ==========

    private SalesCombinationActivityRespVO activity;

}
