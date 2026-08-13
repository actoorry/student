package vip.appap.suxin.module.wms.controller.admin.stock.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 库存快照新增/修改 Request VO")
@Data
public class WmsStockSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6867")
    private Long id;

    @Schema(description = "产品SKU ID，FK -> product_sku.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29235")
    @NotNull(message = "产品SKU ID，FK -> product_sku.id不能为空")
    private Long skuId;

    @Schema(description = "所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）", requiredMode = Schema.RequiredMode.REQUIRED, example = "401")
    @NotNull(message = "所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）不能为空")
    private Long warehouseId;

    @Schema(description = "存储库位ID，FK -> wms_warehouse.id（叶子节点）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13770")
    @NotNull(message = "存储库位ID，FK -> wms_warehouse.id（叶子节点）不能为空")
    private Long locationId;

    @Schema(description = "库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）不能为空")
    private Integer stockMode;

    @Schema(description = "批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号")
    private String batchNo;

    @Schema(description = "生产日期，批次管理时使用")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expiryDate;

    @Schema(description = "库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）不能为空")
    private BigDecimal quantity;

    @Schema(description = "最近入库单价（移动平均），仅入库类单据更新", example = "5850")
    private BigDecimal unitPrice;

}