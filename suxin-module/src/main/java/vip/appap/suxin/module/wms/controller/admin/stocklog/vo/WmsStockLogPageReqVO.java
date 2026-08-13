package vip.appap.suxin.module.wms.controller.admin.stocklog.vo;

import lombok.*;

import java.time.LocalDate;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 库存流水台账分页 Request VO")
@Data
public class WmsStockLogPageReqVO extends PageParam {

    @Schema(description = "产品名称关键词，模糊搜索 product_spu.name")
    private String skuId;

    @Schema(description = "实体仓库ID", example = "32562")
    private Long warehouseId;

    @Schema(description = "来源位置（虚拟仓或实体库位）", example = "24989")
    private Long fromLocationId;

    @Schema(description = "去向位置（虚拟仓或实体库位）", example = "29587")
    private Long toLocationId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "生产日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] productionDate;

    @Schema(description = "有效期至")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expiryDate;

    @Schema(description = "来源业务：order=出入库单 / adjust=盘点调整单", example = "2")
    private String bizType;

    @Schema(description = "业务单据ID（wms_order.id 或 wms_inventory_adjust.id）", example = "26508")
    private Long bizId;

    @Schema(description = "业务单据编号，冗余方便查询")
    private Long bizNo;

    @Schema(description = "本次变动前的库存量")
    private BigDecimal beforeQuantity;

    @Schema(description = "本次变动量。入库为正、出库为负")
    private BigDecimal changeQuantity;

    @Schema(description = "本次变动后的库存量")
    private BigDecimal afterQuantity;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}