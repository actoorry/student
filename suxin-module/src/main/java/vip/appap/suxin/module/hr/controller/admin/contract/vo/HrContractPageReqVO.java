package vip.appap.suxin.module.hr.controller.admin.contract.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 劳动合同分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HrContractPageReqVO extends PageParam {

    @Schema(description = "关联员工", example = "10870")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "合同编号")
    private String contractNo;

    @Schema(description = "甲方")
    private String partyA;

    @Schema(description = "乙方所在部门")
    private String dept;

    @Schema(description = "合同开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "合同结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "合同状态", example = "2")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
