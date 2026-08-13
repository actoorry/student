package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 动态简要信息 Response VO（用于嵌入卡片等场景）")
@Data
@Accessors(chain = true)
public class AppPartnerMomentSimpleRespVO {

    @Schema(description = "动态 ID", example = "1001")
    private Long id;

    @Schema(description = "文字内容", example = "今天天气真好")
    private String content;

    @Schema(description = "图片列表")
    private List<String> imageUrls;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "点赞数", example = "10")
    private Integer likeCount;

    @Schema(description = "评论数", example = "3")
    private Integer commentCount;

}
