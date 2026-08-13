package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 商品页面展示配置更新 Request VO
 */
@Schema(description = "管理后台 - 商品页面展示配置更新 Request VO")
@Data
public class ProductDisplayConfigUpdateReqVO {

    @Schema(description = "展示场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "MEMBER_PAGE")
    @NotBlank(message = "展示场景编码不能为空")
    private String sceneCode;

    @Schema(description = "商品销售分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "90")
    @NotNull(message = "商品销售分类编号不能为空")
    @Positive(message = "商品销售分类编号必须大于 0")
    private Long categoryId;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "开启状态不能为空")
    private Integer status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "备注", example = "会员套餐展示场景")
    private String remark;

    @Schema(description = "期望的更新时间（用于并发控制）", example = "2026-07-10 12:00:00")
    private LocalDateTime updateTime;

}
