package vip.appap.suxin.module.wms.controller.admin.order.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出入库/调拨单据头分页 Request VO")
@Data
public class WmsOrderPageReqVO extends PageParam {

    @Schema(description = "单据编号，Redis INCR 生成")
    private Long no;

    @Schema(description = "单据类型，FK -> wms_order_type.id", example = "29400")
    private Long typeId;

    @Schema(description = "状态：0=草稿 1=待审核 2=已审核 3=已完成 -1=已取消", example = "2")
    private Integer status;

    @Schema(description = "供应商ID，FK -> partner.id。need_supplier=1时必填", example = "21611")
    private Long supplierId;

    @Schema(description = "客户ID，FK -> partner.id。need_customer=1时必填", example = "29199")
    private Long customerId;

    @Schema(description = "来源位置。实体库位或虚拟仓ID", example = "61")
    private Long fromWarehouseId;

    @Schema(description = "去向位置。实体库位或虚拟仓ID", example = "503")
    private Long toWarehouseId;

    @Schema(description = "源单据ID（如来自采购单或销售单）", example = "7205")
    private Long originId;

    @Schema(description = "操作人ID", example = "3223")
    private Long operatorId;

    @Schema(description = "单据日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}