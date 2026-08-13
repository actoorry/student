package vip.appap.suxin.module.wms.controller.admin.ordertype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 出入库单据类型配置新增/修改 Request VO")
@Data
public class WmsOrderTypeSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "25044")
    private Long id;

    @Schema(description = "单据类型名称，如\"采购入库\"", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "单据类型名称，如\"采购入库\"不能为空")
    private String name;

    @Schema(description = "默认来源位置。0=用户自选实体位置，1-6=固定虚拟仓", example = "7957")
    private Long fromLocationId;

    @Schema(description = "默认去向位置。0=用户自选，1-6=固定虚拟仓", example = "23552")
    private Long toLocationId;

    @Schema(description = "库存影响方向：1=入库（增库存） 2=出库（减库存） 3=内部调拨", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "库存影响方向不能为空")
    @Min(value = 1, message = "库存影响方向值不正确")
    @Max(value = 3, message = "库存影响方向值不正确")
    private Integer stockImpact;

    @Schema(description = "是否强制填写供应商：0=否 1=是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否强制填写供应商：0=否 1=是不能为空")
    private Integer needSupplier;

    @Schema(description = "是否强制填写客户：0=否 1=是", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否强制填写客户：0=否 1=是不能为空")
    private Integer needCustomer;

    @Schema(description = "是否启用：0=停用 1=启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用：0=停用 1=启用不能为空")
    private Integer active;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "备注", example = "随便")
    private String remark;

}