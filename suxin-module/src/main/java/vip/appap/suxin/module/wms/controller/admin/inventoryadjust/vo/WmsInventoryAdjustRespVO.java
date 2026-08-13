package vip.appap.suxin.module.wms.controller.admin.inventoryadjust.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 盘点调整记录 Response VO")
@Data
public class WmsInventoryAdjustRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "单据编号，Redis INCR 生成")
    private Long no;

    @Schema(description = "盘点仓库，FK -> wms_warehouse.id（实体仓库）", example = "19979")
    private Long warehouseId;

    @Schema(description = "产品SKU ID，FK -> product_sku.id", example = "4658")
    private Long skuId;

    @Schema(description = "库位ID，FK -> wms_warehouse.id", example = "10410")
    private Long locationId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "生产日期")
    private String productionDate;

    @Schema(description = "有效期至")
    private String expiryDate;

    @Schema(description = "账面数量，系统从wms_stock读取的当前库存")
    private BigDecimal bookQuantity;

    @Schema(description = "实盘数量，用户实际盘点后填入")
    private BigDecimal actualQuantity;

    @Schema(description = "状态：0=草稿 1=已审核", example = "1")
    private Integer status;

    @Schema(description = "盘点类型：1=全盘 2=抽盘 3=手动调整", example = "2")
    private Integer checkType;

    @Schema(description = "盘点时间")
    private LocalDateTime checkTime;

    @Schema(description = "操作人ID", example = "4706")
    private Long operatorId;

    @Schema(description = "备注", example = "你说的对")
    private String remark;
}