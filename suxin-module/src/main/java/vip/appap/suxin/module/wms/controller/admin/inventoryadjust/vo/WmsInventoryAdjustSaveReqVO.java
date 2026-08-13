package vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 盘点调整记录创建/更新 Request VO")
@Data
public class WmsInventoryAdjustSaveReqVO {

    @Schema(description = "主键ID，更新时必填")
    private Long id;

    @Schema(description = "库存记录ID（创建时必填，用于读取账面库存）", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    @NotNull(message = "库存记录ID不能为空", groups = {Create.class})
    private Long stockId;

    @Schema(description = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "实盘数量不能为空")
    @DecimalMin(value = "0", message = "实盘数量不能小于0")
    private BigDecimal actualQuantity;

    @Schema(description = "盘点类型：1=全盘 2=抽盘 3=手动调整", example = "1")
    private Integer checkType;

    @Schema(description = "盘点时间")
    private LocalDateTime checkTime;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    public interface Create {}
}