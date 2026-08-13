package vip.appap.suxin.module.wms.controller.admin.stock.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 库存快照分页 Request VO")
@Data
public class WmsStockPageReqVO extends PageParam {

    @Schema(description = "产品名称关键词，模糊搜索 product_spu.name")
    private String skuId;

    @Schema(description = "所属实体仓库ID，FK -> wms_warehouse.id（parent_id=0）", example = "401")
    private Long warehouseId;

    @Schema(description = "存储库位ID，FK -> wms_warehouse.id（叶子节点）", example = "13770")
    private Long locationId;

    @Schema(description = "库存管控方式：0=不管理 1=批次 2=序列号（冗余自product_spu）")
    private Integer stockMode;

    @Schema(description = "批次号。stock_mode=0时为NULL；stock_mode=1时用户录入；stock_mode=2时存序列号")
    private String batchNo;

    @Schema(description = "生产日期，批次管理时使用")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] productionDate;

    @Schema(description = "有效期至")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expiryDate;

    @Schema(description = "库存数量（最小单位）。正数，允许0（仅记录库位-产品关系）")
    private BigDecimal quantity;

    @Schema(description = "最近入库单价（移动平均），仅入库类单据更新", example = "5850")
    private BigDecimal unitPrice;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}