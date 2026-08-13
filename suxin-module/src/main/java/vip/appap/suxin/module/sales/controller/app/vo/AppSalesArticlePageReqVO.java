package vip.appap.suxin.module.sales.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "应用 App - 文章的分页 Request VO")
@Data
public class AppSalesArticlePageReqVO extends PageParam {

    @Schema(description = "分类编号", example = "2048")
    private Long categoryId;

    @Schema(description = "省份区域编号（战友会使用）", example = "210000")
    private Integer stateId;

}
