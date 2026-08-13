package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 会员签到配置 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PartnerSignInConfigBaseVO {

    @Schema(description = "第几天", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "第几天不能为空")
    private Integer day;

    @Schema(description = "奖励积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "奖励积分不能为空")
    private Integer point;

    @Schema(description = "奖励经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "奖励经验不能为空")
    private Integer experience;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
