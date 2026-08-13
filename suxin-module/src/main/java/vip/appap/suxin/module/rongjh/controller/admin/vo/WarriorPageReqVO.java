package vip.appap.suxin.module.rongjh.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 战友会身份分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WarriorPageReqVO extends PageParam {

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "类型: SELF/MARTYR/SACRIFICE/ILLNESS/FAMILY", example = "SELF")
    private String type;

    @Schema(description = "类型分组: SELF-战友会 / SANSHU-三属及军人家属", example = "SELF")
    private String typeGroup;

    @Schema(description = "状态: 0待审核 1已通过 2已驳回", example = "0")
    private Integer status;

    @Schema(description = "省份区域编号", example = "210000")
    private Integer stateId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
