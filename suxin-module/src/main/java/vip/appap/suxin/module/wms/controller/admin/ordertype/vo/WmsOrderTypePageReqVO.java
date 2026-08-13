package vip.appap.suxin.module.wms.controller.admin.ordertype.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 出入库单据类型配置分页 Request VO")
@Data
public class WmsOrderTypePageReqVO extends PageParam {

    @Schema(description = "单据类型名称，如/\"采购入库/\"", example = "赵六")
    private String name;

    @Schema(description = "默认来源位置。0=用户自选实体位置，1-6=固定虚拟仓", example = "7957")
    private Long fromLocationId;

    @Schema(description = "默认去向位置。0=用户自选，1-6=固定虚拟仓", example = "23552")
    private Long toLocationId;

    @Schema(description = "库存影响方向：1=入库（增库存） 2=出库（减库存）")
    private Integer stockImpact;

    @Schema(description = "是否强制填写供应商：0=否 1=是")
    private Integer needSupplier;

    @Schema(description = "是否强制填写客户：0=否 1=是")
    private Integer needCustomer;

    @Schema(description = "是否启用：0=停用 1=启用")
    private Integer active;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}