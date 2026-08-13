package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 会员地址分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerAddressPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "地址类型：0-会员收件地址 1-商户寄件地址", example = "1")
    private Integer type;

    @Schema(description = "收件人名称", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
