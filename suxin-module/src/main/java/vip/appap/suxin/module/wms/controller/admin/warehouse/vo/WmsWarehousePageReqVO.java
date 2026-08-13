package vip.appap.suxin.module.wms.controller.admin.warehouse.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 仓库位置表（仓库/库区/库位树形结构）分页 Request VO")
@Data
public class WmsWarehousePageReqVO extends PageParam {

    @Schema(description = "上级节点。0=实体仓库根节点；等于自身id=虚拟仓（不可移动）", example = "8128")
    private Long parentId;

    @Schema(description = "名称，如\"原料仓\"、\"A区\"、\"A-01\"", example = "张三")
    private String name;

    @Schema(description = "编码，非必填，无唯一约束，仅用于展示或外部系统对接")
    private String code;

    @Schema(description = "归属城市区县ID，用于按区域筛选仓库", example = "17367")
    private Integer areaId;

    @Schema(description = "详细地址，实体仓库层级填写")
    private String address;

    @Schema(description = "仓库负责人ID，FK -> partner.id", example = "9267")
    private Long partnerId;

    @Schema(description = "状态：0=禁用 1=启用", example = "1")
    private Integer status;

    @Schema(description = "同级排序，越小越前")
    private Integer sort;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}