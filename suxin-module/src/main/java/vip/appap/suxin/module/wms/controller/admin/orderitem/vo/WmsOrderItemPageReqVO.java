package vip.appap.suxin.module.wms.controller.admin.orderitem.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出入库/调拨单据明细分页 Request VO")
@Data
public class WmsOrderItemPageReqVO extends PageParam {

    @Schema(description = "出入库单ID，FK -> wms_order.id", example = "27317")
    private Long orderId;

    @Schema(description = "产品名称关键词，模糊搜索 product_spu.name")
    private String skuId;

    @Schema(description = "来源明细ID（如purchase_order_item.id），用于回写已收数量", example = "21550")
    private Long originId;

    @Schema(description = "来源位置。实体库位或虚拟仓ID", example = "19132")
    private Long fromLocationId;

    @Schema(description = "去向位置。实体库位或虚拟仓ID", example = "28176")
    private Long toLocationId;

    @Schema(description = "批次号。stock_mode≠0时必填，入库手动录入，出库按FIFO自动匹配")
    private String batchNo;

    @Schema(description = "生产日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] productionDate;

    @Schema(description = "有效期至")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expiryDate;

    @Schema(description = "移动数量（绝对值）。入库=入库量，出库=出库量")
    private BigDecimal quantity;

    @Schema(description = "单价，仅入库时可选填写", example = "21758")
    private BigDecimal unitPrice;

    @Schema(description = "行排序")
    private Integer sort;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}