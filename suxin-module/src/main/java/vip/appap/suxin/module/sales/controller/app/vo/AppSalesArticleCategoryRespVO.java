package vip.appap.suxin.module.sales.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "应用 App - 文章分类 Response VO")
@Data
public class AppSalesArticleCategoryRespVO {

    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "技术")
    private String name;

    @Schema(description = "分类图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.appap.vip/1.png")
    private String picUrl;

}
