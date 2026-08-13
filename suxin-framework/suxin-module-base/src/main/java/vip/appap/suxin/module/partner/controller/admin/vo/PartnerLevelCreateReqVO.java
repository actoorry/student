package vip.appap.suxin.module.partner.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 会员等级创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerLevelCreateReqVO extends PartnerLevelBaseVO {

}
