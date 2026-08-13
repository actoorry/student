package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 积分商城活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesPointActivityPageReqVO extends PageParam {

    @Schema(description = "活动状态", example = "2")
    private Integer status;

}