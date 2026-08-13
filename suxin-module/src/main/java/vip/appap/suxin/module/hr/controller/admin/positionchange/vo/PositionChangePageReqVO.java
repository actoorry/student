package vip.appap.suxin.module.hr.controller.admin.positionchange.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PositionChangePageReqVO extends PageParam {

    private Long partnerId;
    private String name;
    private String changeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] startDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
