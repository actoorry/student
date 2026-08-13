package vip.appap.suxin.module.hr.controller.admin.resume.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 履历分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ResumePageReqVO extends PageParam {

    @Schema(description = "关联员工", example = "16087")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "所在部门")
    private String dept;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
