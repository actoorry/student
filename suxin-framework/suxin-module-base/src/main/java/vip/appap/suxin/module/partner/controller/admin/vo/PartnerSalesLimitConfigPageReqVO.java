package vip.appap.suxin.module.partner.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 客户限制配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PartnerSalesLimitConfigPageReqVO extends PageParam {

    @Schema(description = "规则类型", example = "1")
    private Integer type;

}
