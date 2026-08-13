package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 会员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerMemberPageReqVO extends PageParam {

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "会员类型", example = "1")
    private Integer memberType;

    @Schema(description = "会员状态", example = "1")
    private Integer status;

    @Schema(description = "手机号", example = "15601691300")
    private String mobile;

    @Schema(description = "用户昵称", example = "李四")
    private String nickname;

    @Schema(description = "会员等级编号", example = "1")
    private Long customerLevel;

    @Schema(description = "用户分组编号", example = "1")
    private Long groupId;

    @Schema(description = "会员标签编号列表", example = "[1, 2]")
    private List<Long> tagIds;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
