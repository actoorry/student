package vip.appap.suxin.module.marriage.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "用户 App - 动态分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppPartnerMomentPageReqVO extends PageParam {

    @Schema(description = "是否只看我关注的人", example = "false")
    private Boolean following;

    @Schema(description = "指定用户 ID（查看某个用户的动态）", example = "1001")
    private Long partnerId;

}
