package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import vip.appap.suxin.framework.common.validation.InEnum;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleStatusEnum;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleTypeEnum;
import vip.appap.suxin.module.sales.enums.SalesAfterSaleWayEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 交易售后分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesAfterSalePageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "售后流水号", example = "202211190847450020500077")
    private String no;

    @Schema(description = "售后状态", example = "10")
    @InEnum(value = SalesAfterSaleStatusEnum.class, message = "售后状态必须是 {value}")
    private Integer status;

    @Schema(description = "售后类型", example = "20")
    @InEnum(value = SalesAfterSaleTypeEnum.class, message = "售后类型必须是 {value}")
    private Integer type;

    @Schema(description = "售后方式", example = "10")
    @InEnum(value = SalesAfterSaleWayEnum.class, message = "售后方式必须是 {value}")
    private Integer way;

    @Schema(description = "订单编号", example = "18078")
    private String orderNo;

    @Schema(description = "商品 SPU 名称", example = "李四")
    private String spuName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
