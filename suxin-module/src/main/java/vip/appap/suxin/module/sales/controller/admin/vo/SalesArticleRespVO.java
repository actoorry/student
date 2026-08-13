package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 文章管理 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesArticleRespVO extends SalesArticleBaseVO {

    @Schema(description = "文章编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8606")
    private Long id;

    @Schema(description = "浏览量", requiredMode = Schema.RequiredMode.REQUIRED, example = "99999")
    private Integer browseCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
