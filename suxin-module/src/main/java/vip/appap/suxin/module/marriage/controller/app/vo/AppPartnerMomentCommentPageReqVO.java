package vip.appap.suxin.module.marriage.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 动态评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPartnerMomentCommentPageReqVO extends PageParam {

    @Schema(description = "动态 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "动态 ID 不能为空")
    private Long momentId;

}
