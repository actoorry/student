package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 砍价活动分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesBargainActivityPageReqVO extends PageParam {

    @Schema(description = "砍价名称", example = "赵六")
    private String name;

    @Schema(description = "活动状态", example = "0")
    private Integer status;

}
