package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 App - 动态点赞 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppPartnerMomentLikeRespVO {

    private Boolean liked;
    private Integer likeCount;

}
