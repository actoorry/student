package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 合作伙伴分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerPageReqVO extends PageParam {

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "是否客户", example = "true")
    private Boolean customer;

    @Schema(description = "是否供应商", example = "true")
    private Boolean supplier;

    @Schema(description = "是否公司", example = "true")
    private Boolean company;

}
