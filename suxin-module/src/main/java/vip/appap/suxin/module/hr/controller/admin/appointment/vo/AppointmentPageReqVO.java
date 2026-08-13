package vip.appap.suxin.module.hr.controller.admin.appointment.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 岗位聘任分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppointmentPageReqVO extends PageParam {

    @Schema(description = "员工 partner.id")
    private Long partnerId;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "岗位类别")
    private String postCategory;

    @Schema(description = "岗位等级")
    private String postLevel;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "是否当前有效聘任")
    private Integer isCurrent;

    @Schema(description = "到期日范围")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate[] endDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
