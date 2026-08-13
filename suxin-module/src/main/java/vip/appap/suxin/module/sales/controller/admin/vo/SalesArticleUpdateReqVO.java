package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 文章管理更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesArticleUpdateReqVO extends SalesArticleBaseVO {

    @Schema(description = "文章编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8606")
    @NotNull(message = "文章编号不能为空")
    private Long id;

}
