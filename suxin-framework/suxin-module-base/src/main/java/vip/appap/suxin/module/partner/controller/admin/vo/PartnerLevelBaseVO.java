package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 会员等级 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class PartnerLevelBaseVO {

    @Schema(description = "等级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "黄金会员")
    @NotNull(message = "等级名称不能为空")
    private String name;

    @Schema(description = "等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "等级不能为空")
    private Integer level;

    @Schema(description = "升级经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    @NotNull(message = "升级经验不能为空")
    private Integer experience;

    @Schema(description = "享受折扣", requiredMode = Schema.RequiredMode.REQUIRED, example = "95")
    @NotNull(message = "享受折扣不能为空")
    private Integer discountPercent;

    @Schema(description = "等级图标", example = "https://www.appap.vip/level.png")
    private String icon;

    @Schema(description = "等级背景图", example = "https://www.appap.vip/level-bg.png")
    private String backgroundUrl;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
