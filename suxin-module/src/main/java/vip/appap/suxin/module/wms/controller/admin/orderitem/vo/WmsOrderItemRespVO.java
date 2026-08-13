package vip.appap.suxin.module.wms.controller.admin.orderitem.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 出入库/调拨单据明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOrderItemRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15588")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "出入库单ID，FK -> wms_order.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "27317")
    @ExcelProperty("出入库单ID，FK -> wms_order.id")
    private Long orderId;

    @Schema(description = "产品SKU ID，FK -> product_sku.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30199")
    @ExcelProperty("产品SKU ID，FK -> product_sku.id")
    private Long skuId;

    @Schema(description = "来源明细ID（如purchase_order_item.id），用于回写已收数量", example = "21550")
    @ExcelProperty("来源明细ID（如purchase_order_item.id），用于回写已收数量")
    private Long originId;

    @Schema(description = "来源位置。实体库位或虚拟仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "19132")
    @ExcelProperty("来源位置。实体库位或虚拟仓ID")
    private Long fromLocationId;

    @Schema(description = "去向位置。实体库位或虚拟仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28176")
    @ExcelProperty("去向位置。实体库位或虚拟仓ID")
    private Long toLocationId;

    @Schema(description = "批次号。stock_mode≠0时必填，入库手动录入，出库按FIFO自动匹配")
    @ExcelProperty("批次号。stock_mode≠0时必填，入库手动录入，出库按FIFO自动匹配")
    private String batchNo;

    @Schema(description = "生产日期")
    @ExcelProperty("生产日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate productionDate;

    @Schema(description = "有效期至")
    @ExcelProperty("有效期至")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expiryDate;

    @Schema(description = "移动数量（绝对值）。入库=入库量，出库=出库量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("移动数量（绝对值）。入库=入库量，出库=出库量")
    private BigDecimal quantity;

    @Schema(description = "单价，仅入库时可选填写", example = "21758")
    @ExcelProperty("单价，仅入库时可选填写")
    private BigDecimal unitPrice;

    @Schema(description = "行排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("行排序")
    private Integer sort;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
