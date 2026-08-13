package vip.appap.suxin.module.hr.controller.admin.outbound.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 外出记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OutboundPageReqVO extends PageParam {

    @Schema(description = "关联员工 partner.id")
    private Long partnerId;

    @Schema(description = "员工姓名（模糊）")
    private String name;

    @Schema(description = "外出类型 hr_outbound_type", example = "rural_support")
    private String recordType;

    @Schema(description = "是否计入汇总 1是 0否")
    private Integer effective;

    @Schema(description = "开始日期范围")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] startDate;

    @Schema(description = "创建时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
