package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理后台 - 商品页面展示配置 Response VO
 */
@Schema(description = "管理后台 - 商品页面展示配置 Response VO")
@Data
public class ProductDisplayConfigListRespVO {

    @Schema(description = "展示场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "MEMBER_PAGE")
    private String sceneCode;

    @Schema(description = "展示场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "会员套餐页面")
    private String sceneName;

    @Schema(description = "配置状态", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "ACTIVE", allowableValues = {"UNCONFIGURED", "ACTIVE", "DISABLED", "INVALID_CATEGORY", "MULTIPLE_CATEGORIES"})
    private String configState;

    @Schema(description = "存储的商品销售分类编号列表（原始值）", example = "[90]")
    private List<Long> storedCategoryIds;

    @Schema(description = "当前有效的商品销售分类编号", example = "90")
    private Long categoryId;

    @Schema(description = "当前有效的商品销售分类名称", example = "会员产品")
    private String categoryName;

    @Schema(description = "开启状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer sort;

    @Schema(description = "备注", example = "会员套餐展示场景")
    private String remark;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
