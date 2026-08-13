package vip.appap.suxin.module.wms.controller.admin.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 仓库位置表（仓库/库区/库位树形结构） Response VO")
@Data
@ExcelIgnoreUnannotated
public class WmsWarehouseRespVO {

    @Schema(description = "主键（1-6保留给虚拟仓，用户数据从100起）", requiredMode = Schema.RequiredMode.REQUIRED, example = "240")
    @ExcelProperty("主键（1-6保留给虚拟仓，用户数据从100起）")
    private Long id;

    @Schema(description = "上级节点。0=实体仓库根节点；等于自身id=虚拟仓（不可移动）", requiredMode = Schema.RequiredMode.REQUIRED, example = "8128")
    @ExcelProperty("上级节点。0=实体仓库根节点；等于自身id=虚拟仓（不可移动）")
    private Long parentId;

    @Schema(description = "名称，如\"原料仓\"、\"A区\"、\"A-01\"", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("名称，如\"原料仓\"、\"A区\"、\"A-01\"")
    private String name;

    @Schema(description = "编码，非必填，无唯一约束，仅用于展示或外部系统对接")
    @ExcelProperty("编码，非必填，无唯一约束，仅用于展示或外部系统对接")
    private String code;

    @Schema(description = "归属城市区县ID，FK -> Area.getId()", example = "17367")
    @ExcelProperty("归属城市区县ID")
    private Integer areaId;

    @Schema(description = "归属城市区县名称，通过AreaUtils.format(areaId)获取", example = "四川省/成都市/高新区")
    @ExcelProperty("归属城市区县名称")
    private String areaName;

    @Schema(description = "详细地址，实体仓库层级填写")
    @ExcelProperty("详细地址，实体仓库层级填写")
    private String address;

    @Schema(description = "仓库负责人ID，FK -> partner.id；仅允许类型为【公司】的客商；仅一级实体仓库可配置", example = "9267")
    @ExcelProperty("仓库负责人ID")
    private Long partnerId;

    @Schema(description = "状态：0=禁用 1=启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：0=禁用 1=启用")
    private Integer status;

    @Schema(description = "同级排序，越小越前", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("同级排序，越小越前")
    private Integer sort;

    @Schema(description = "位置类型：0=view 1=warehouse 2=area 3=location 4=supplier 5=customer 6=inventory 7=scrap 8=production 9=transit", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("位置类型")
    private Integer locType;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

}
