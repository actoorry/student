package vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 盘点调整记录分页 Request VO")
@Data
public class WmsInventoryAdjustPageReqVO extends PageParam {

    @Schema(description = "单据编号")
    private Long no;

    @Schema(description = "盘点仓库，FK -> wms_warehouse.id", example = "19979")
    private Long warehouseId;

    @Schema(description = "状态：0=草稿 1=已审核", example = "1")
    private Integer status;

    @Schema(description = "盘点类型：1=全盘 2=抽盘 3=手动调整", example = "2")
    private Integer checkType;
}