package vip.appap.suxin.module.wms.controller.admin.stock.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 库存快照 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "6867")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "产品SKU ID，FK -> product_sku.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29235")
    @ExcelProperty("产品SKU ID，FK -> product_sku.id")
    private Long skuId;

    @Schema(description = "所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）", requiredMode = Schema.RequiredMode.REQUIRED, example = "401")
    @ExcelProperty("所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）")
    private Long warehouseId;

    @Schema(description = "存储库位ID，FK -> wms_warehouse.id（叶子节点）", requiredMode = Schema.RequiredMode.REQUIRED, example = "13770")
    @ExcelProperty("存储库位ID，FK -> wms_warehouse.id（叶子节点）")
    private Long locationId;

    @Schema(description = "库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）")
    private Integer stockMode;

    @Schema(description = "批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号")
    @ExcelProperty("批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号")
    private String batchNo;

    @Schema(description = "生产日期，批次管理时使用")
    @ExcelProperty("生产日期，批次管理时使用")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    @ExcelProperty("有效期至")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expiryDate;

    @Schema(description = "库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）")
    private BigDecimal quantity;

    @Schema(description = "预占数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预占数量")
    private BigDecimal reservedQuantity;

    @Schema(description = "最近入库单价（移动平均），仅入库类单据更新", example = "5850")
    @ExcelProperty("最近入库单价（移动平均），仅入库类单据更新")
    private BigDecimal unitPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
