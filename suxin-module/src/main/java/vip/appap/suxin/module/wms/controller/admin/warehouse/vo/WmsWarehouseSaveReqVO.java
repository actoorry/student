package vip.appap.suxin.module.wms.controller.admin.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 仓库位置表（仓库/库区/库位树形结构）新增/修改 Request VO")
@Data
public class WmsWarehouseSaveReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "上级节点。0=根节点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "上级节点不能为空")
    private Long parentId;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "编码，非必填")
    private String code;

    @Schema(description = "位置类型：0=view 虚拟 1=warehouse 仓库 2=area 库区 3=location 库位\n" +
        "4=supplier 供应商 5=customer 客户 6=inventory 盘点差异 7=scrap 报废 8=production 生产 9=transit 在途", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "位置类型不能为空")
    @Min(value = 0, message = "位置类型值不正确")
    @Max(value = 9, message = "位置类型值不正确")
    private Integer locType;

    @Schema(description = "归属城市区县ID，FK -> Area.getId()")
    private Integer areaId;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "仓库负责人ID，FK -> partner.id；仅允许类型为【公司】的客商；仅一级实体仓库可配置")
    private Long partnerId;

    @Schema(description = "状态：0=禁用 1=启用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "同级排序，越小越前", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

}