package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 动态评论 Response VO")
@Data
@Accessors(chain = true)
public class AppPartnerMomentCommentRespVO {

    private Long id;
    private Long momentId;
    private Long partnerId;
    private String nickname;
    private String avatar;
    private Boolean verified;
    private Long parentId;
    private Long replyToPartnerId;
    private String replyToNickname;
    private String content;
    private Integer likeCount;
    private LocalDateTime createTime;
    private List<AppPartnerMomentCommentRespVO> replies;

}
