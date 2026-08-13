package vip.appap.suxin.module.marriage.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 首页推荐会员分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPartnerMarriageProfilePageReqVO extends PageParam {

    @Schema(description = "是否仅推荐已实名人物", example = "false")
    private Boolean realVerifiedOnly;

    @Schema(description = "是否要求人物有背景图", example = "false")
    private Boolean backgroundImageRequired;

    @Schema(description = "是否仅推荐异性", example = "false")
    private Boolean oppositeSexOnly;
}
