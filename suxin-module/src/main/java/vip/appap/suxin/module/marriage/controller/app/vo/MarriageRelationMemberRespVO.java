package vip.appap.suxin.module.marriage.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Schema(description = "App - Marriage relation member Response VO")
@Data
@Accessors(chain = true)
public class MarriageRelationMemberRespVO {

    @Schema(description = "Partner id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long partnerId;

    @Schema(description = "Nickname", requiredMode = Schema.RequiredMode.REQUIRED, example = "微信用户")
    private String nickname;

    @Schema(description = "Avatar")
    private String avatar;

    @Schema(description = "Age", example = "26")
    private Integer age;

    @Schema(description = "City", example = "深圳")
    private String city;

    @Schema(description = "Profile bio")
    private String bio;

    @Schema(description = "Whether current user follows this member", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean followed;

    @Schema(description = "Whether both users follow each other", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean mutual;

    @Schema(description = "Relation create time")
    private LocalDateTime createTime;

}
