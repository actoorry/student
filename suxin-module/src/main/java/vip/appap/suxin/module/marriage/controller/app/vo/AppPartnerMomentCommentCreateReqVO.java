package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户 App - 创建动态评论 Request VO")
@Data
public class AppPartnerMomentCommentCreateReqVO {

    @Schema(description = "动态 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "动态 ID 不能为空")
    private Long momentId;

    @Schema(description = "父评论 ID，0 表示一级评论", example = "0")
    private Long parentId;

    @Schema(description = "被回复用户 ID", example = "1024")
    private Long replyToPartnerId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过 500 个字符")
    private String content;

}
