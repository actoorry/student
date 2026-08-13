package vip.appap.suxin.module.hr.controller.admin.certificate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 证书推送日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CertificateNotifyLogPageReqVO extends PageParam {

    @Schema(description = "通知类型：expired / expire_90 / expire_60 / expire_30 / assessment_60")
    private String notifyType;

    @Schema(description = "推送状态：success / failed / skipped")
    private String pushStatus;

    @Schema(description = "目标类型：self / dept_leader / hr")
    private String targetType;

    @Schema(description = "目标用户 id")
    private Long targetUserId;

    @Schema(description = "证书 id")
    private Long certificateId;

    @Schema(description = "目标用户姓名（模糊）")
    private String name;

    @Schema(description = "推送时间范围")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] notifyTime;

}
