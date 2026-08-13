package vip.appap.suxin.module.marriage.controller.app.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "App - Marriage notify message page Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MarriageNotifyMessagePageReqVO extends PageParam {

    @Schema(description = "Read status", example = "false")
    private Boolean readStatus;

}
