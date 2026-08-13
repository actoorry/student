package vip.appap.suxin.module.wms.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

import vip.appap.suxin.module.wms.controller.admin.orderitem.vo.WmsOrderItemSaveReqVO;

@Schema(description = "管理后台 - 出入库/调拨单据头新增/修改 Request VO")
@Data
public class WmsOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17949")
    private Long id;

    @Schema(description = "单据编号，Redis INCR 生成（新增时不传，后端自动生成）")
    private Long no;

    @Schema(description = "单据类型，FK -> wms_order_type.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "29400")
    @NotNull(message = "单据类型，FK -> wms_order_type.id不能为空")
    private Long typeId;

    @Schema(description = "状态：0=草稿 1=待审核 2=已审核 3=已完成 -1=已取消（新增时默认0=草稿）")
    private Integer status;

    @Schema(description = "供应商ID，FK -> partner.id。need_supplier=1时必填", example = "21611")
    private Long supplierId;

    @Schema(description = "客户ID，FK -> partner.id。need_customer=1时必填", example = "29199")
    private Long customerId;

    @Schema(description = "来源位置。实体库位或虚拟仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "61")
    @NotNull(message = "来源位置。实体库位或虚拟仓ID不能为空")
    private Long fromWarehouseId;

    @Schema(description = "去向位置。实体库位或虚拟仓ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "503")
    @NotNull(message = "去向位置。实体库位或虚拟仓ID不能为空")
    private Long toWarehouseId;

    @Schema(description = "源单据ID（如来自采购单或销售单）", example = "7205")
    private Long originId;

    @Schema(description = "操作人ID", example = "3223")
    private Long operatorId;

    @Schema(description = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime orderTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "单据明细行列表")
    @Valid
    private List<WmsOrderItemSaveReqVO> items;

}