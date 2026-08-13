package vip.appap.suxin.module.rongjh.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 爱心帮扶申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HelpPageReqVO extends PageParam {

    @Schema(description = "困难人姓名")
    private String name;

    @Schema(description = "困难人联系电话")
    private String phone;

    @Schema(description = "申请人会员手机号")
    private String mobile;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回 3已撤销", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
