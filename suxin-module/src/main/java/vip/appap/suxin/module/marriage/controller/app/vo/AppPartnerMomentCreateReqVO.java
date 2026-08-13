package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 发布动态 Request VO")
@Data
public class AppPartnerMomentCreateReqVO {

    @Schema(description = "动态文字", example = "今天阳光很好")
    @Size(max = 1000, message = "动态文字不能超过 1000 个字符")
    private String content;

    @Schema(description = "图片 URL 列表，最多 9 张")
    @Size(max = 9, message = "图片最多 9 张")
    private List<String> imageUrls;

}
