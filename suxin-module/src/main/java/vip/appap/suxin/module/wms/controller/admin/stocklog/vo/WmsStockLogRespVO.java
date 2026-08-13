package vip.appap.suxin.module.wms.controller.admin.stocklog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 库存流水台账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsStockLogRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10525")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "产品SKU ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12940")
    @ExcelProperty("产品SKU ID")
    private Long skuId;

    @Schema(description = "实体仓库ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "32562")
    @ExcelProperty("实体仓库ID")
    private Long warehouseId;

    @Schema(description = "来源位置（虚拟仓或实体库位）", requiredMode = Schema.RequiredMode.REQUIRED, example = "24989")
    @ExcelProperty("来源位置（虚拟仓或实体库位）")
    private Long fromLocationId;

    @Schema(description = "去向位置（虚拟仓或实体库位）", requiredMode = Schema.RequiredMode.REQUIRED, example = "29587")
    @ExcelProperty("去向位置（虚拟仓或实体库位）")
    private Long toLocationId;

    @Schema(description = "批次号")
    @ExcelProperty("批次号")
    private String batchNo;

    @Schema(description = "生产日期")
    @ExcelProperty("生产日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    @ExcelProperty("有效期至")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expiryDate;

    @Schema(description = "来源业务：order=出入库单 / adjust=盘点调整单", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("来源业务：order=出入库单 / adjust=盘点调整单")
    private String bizType;

    @Schema(description = "业务单据ID（wms_order.id 或 wms_inventory_adjust.id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "26508")
    @ExcelProperty("业务单据ID（wms_order.id 或 wms_inventory_adjust.id）")
    private Long bizId;

    @Schema(description = "业务单据编号，冗余方便查询", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("业务单据编号，冗余方便查询")
    private Long bizNo;

    @Schema(description = "本次变动前的库存量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本次变动前的库存量")
    private BigDecimal beforeQuantity;

    @Schema(description = "本次变动量。入库为正、出库为负", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本次变动量。入库为正、出库为负")
    private BigDecimal changeQuantity;

    @Schema(description = "本次变动后的库存量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本次变动后的库存量")
    private BigDecimal afterQuantity;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
