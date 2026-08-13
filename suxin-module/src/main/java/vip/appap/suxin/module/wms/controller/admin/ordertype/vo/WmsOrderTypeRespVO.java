package vip.appap.suxin.module.wms.controller.admin.ordertype.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 出入库单据类型配置 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsOrderTypeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25044")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "单据类型名称，如\"采购入库\"", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("单据类型名称，如\"采购入库\"")
    private String name;

    @Schema(description = "默认来源位置。0=用户自选实体位置，1-6=固定虚拟仓", example = "7957")
    @ExcelProperty("默认来源位置。0=用户自选实体位置，1-6=固定虚拟仓")
    private Long fromLocationId;

    @Schema(description = "默认去向位置。0=用户自选，1-6=固定虚拟仓", example = "23552")
    @ExcelProperty("默认去向位置。0=用户自选，1-6=固定虚拟仓")
    private Long toLocationId;

    @Schema(description = "库存影响方向：1=入库（增库存） 2=出库（减库存） 3=内部调拨", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库存影响方向：1=入库 2=出库 3=内部调拨")
    private Integer stockImpact;

    @Schema(description = "是否强制填写供应商：0=否 1=是", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否强制填写供应商：0=否 1=是")
    private Integer needSupplier;

    @Schema(description = "是否强制填写客户：0=否 1=是", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否强制填写客户：0=否 1=是")
    private Integer needCustomer;

    @Schema(description = "是否启用：0=停用 1=启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否启用：0=停用 1=启用")
    private Integer active;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
