package vip.appap.suxin.module.wms.controller.admin.stocklog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 库存流水台账新增/修改 Request VO")
@Data
public class WmsStockLogSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10525")
    private Long id;

    @Schema(description = "产品SKU ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12940")
    @NotNull(message = "产品SKU ID不能为空")
    private Long skuId;

    @Schema(description = "实体仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32562")
    @NotNull(message = "实体仓库ID不能为空")
    private Long warehouseId;

    @Schema(description = "来源位置（虚拟仓或实体库位）", requiredMode = Schema.RequiredMode.REQUIRED, example = "24989")
    @NotNull(message = "来源位置（虚拟仓或实体库位）不能为空")
    private Long fromLocationId;

    @Schema(description = "去向位置（虚拟仓或实体库位）", requiredMode = Schema.RequiredMode.REQUIRED, example = "29587")
    @NotNull(message = "去向位置（虚拟仓或实体库位）不能为空")
    private Long toLocationId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "生产日期")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    private LocalDate expiryDate;

    @Schema(description = "来源业务：order=出入库单 / adjust=盘点调整单", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "来源业务：order=出入库单 / adjust=盘点调整单不能为空")
    private String bizType;

    @Schema(description = "业务单据ID（wms_order.id 或 wms_inventory_adjust.id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "26508")
    @NotNull(message = "业务单据ID（wms_order.id 或 wms_inventory_adjust.id）不能为空")
    private Long bizId;

    @Schema(description = "业务单据编号，冗余方便查询", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "业务单据编号，冗余方便查询不能为空")
    private Long bizNo;

    @Schema(description = "本次变动前的库存量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本次变动前的库存量不能为空")
    private BigDecimal beforeQuantity;

    @Schema(description = "本次变动量。入库为正、出库为负", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本次变动量。入库为正、出库为负不能为空")
    private BigDecimal changeQuantity;

    @Schema(description = "本次变动后的库存量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "本次变动后的库存量不能为空")
    private BigDecimal afterQuantity;

}