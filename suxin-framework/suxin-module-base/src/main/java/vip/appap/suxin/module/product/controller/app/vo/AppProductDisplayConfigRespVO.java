package vip.appap.suxin.module.product.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Schema(description = "用户 APP - 商品展示配置 Response VO")
@Data
@Accessors(chain = true)
public class AppProductDisplayConfigRespVO {

    @Schema(description = "展示场景编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "EMOTION_COURSE_PAGE")
    private String sceneCode;

    @Schema(description = "启用的商品销售分类编号")
    private List<Long> categoryIds;

}
