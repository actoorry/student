package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 会员签到配置保存 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerSignInConfigSaveReqVO extends PartnerSignInConfigBaseVO {

    @Schema(description = "编号，新增时为空", example = "1")
    private Integer id;

}
