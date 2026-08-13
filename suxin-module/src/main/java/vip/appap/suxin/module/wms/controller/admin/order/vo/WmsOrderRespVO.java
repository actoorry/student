package vip.appap.suxin.module.wms.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 出入库/调拨单据头 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17949")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据编号，Redis INCR 生成", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("单据编号，Redis INCR 生成")
    private Long no;

    @Schema(description = "单据类型，FK -> wms_order_type.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29400")
    @ExcelProperty("单据类型，FK -> wms_order_type.id")
    private Long typeId;

    @Schema(description = "状态：0=草稿 1=待审核 2=已审核 3=已完成 -1=已取消", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：0=草稿 1=待审核 2=已审核 3=已完成 -1=已取消")
    private Integer status;

    @Schema(description = "供应商ID，FK -> partner.id。need_supplier=1时必填", example = "21611")
    @ExcelProperty("供应商ID，FK -> partner.id。need_supplier=1时必填")
    private Long supplierId;

    @Schema(description = "客户ID，FK -> partner.id。need_customer=1时必填", example = "29199")
    @ExcelProperty("客户ID，FK -> partner.id。need_customer=1时必填")
    private Long customerId;

    @Schema(description = "来源位置。实体库位或虚拟仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "61")
    @ExcelProperty("来源位置。实体库位或虚拟仓ID")
    private Long fromWarehouseId;

    @Schema(description = "去向位置。实体库位或虚拟仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "503")
    @ExcelProperty("去向位置。实体库位或虚拟仓ID")
    private Long toWarehouseId;

    @Schema(description = "源单据ID（如来自采购单或销售单）", example = "7205")
    @ExcelProperty("源单据ID（如来自采购单或销售单）")
    private Long originId;

    @Schema(description = "操作人ID", example = "3223")
    @ExcelProperty("操作人ID")
    private Long operatorId;

    @Schema(description = "单据日期")
    @ExcelProperty("单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime orderTime;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}