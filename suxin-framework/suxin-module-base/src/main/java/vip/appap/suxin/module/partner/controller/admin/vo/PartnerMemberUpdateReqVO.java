package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 会员更新 Request VO")
@Data
public class PartnerMemberUpdateReqVO {

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23788")
    @NotNull(message = "会员ID不能为空")
    private Long id;

    @Schema(description = "会员等级编号", example = "1")
    private Long customerLevel;

    @Schema(description = "用户分组编号", example = "1")
    private Long groupId;

    @Schema(description = "会员标签（逗号分隔）", example = "1,2,3")
    private String tagIds;

}
