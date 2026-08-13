package vip.appap.suxin.module.hr.controller.admin.certificate.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 人员证书分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CertificatePageReqVO extends PageParam {

    @Schema(description = "关联员工", example = "10870")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "证书类型", example = "ys_zyz")
    private String certificateType;

    @Schema(description = "科室（system_dept.id）")
    private Long dept;

    @Schema(description = "到期日期范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] expireDate;

    @Schema(description = "计算状态筛选：valid / expiring / expiring_60 / expiring_90 / expired / assessment_overdue / assessment_due_60", example = "expired")
    private String status;

    @Schema(description = "是否缺附件：true 仅查无附件", example = "false")
    private Boolean missingAttachment;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
