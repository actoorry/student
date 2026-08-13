package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "用户 App - 动态 Response VO")
@Data
@Accessors(chain = true)
public class AppPartnerMomentRespVO {

    private Long id;
    private Long partnerId;
    private String avatar;
    private String nickname;
    private Integer realVerified;
    private String verifiedLabel;
    private Boolean verified;
    private LocalDateTime publishTime;
    private Boolean followed;
    private Boolean mine;
    private String content;
    private List<String> imageUrls;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;

}
