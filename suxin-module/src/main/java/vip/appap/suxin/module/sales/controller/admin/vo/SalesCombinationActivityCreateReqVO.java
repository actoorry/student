package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.module.sales.controller.admin.vo.SalesCombinationProductBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.Valid;
import java.util.List;

@Schema(description = "管理后台 - 拼团活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesCombinationActivityCreateReqVO extends SalesCombinationActivityBaseVO {

    @Schema(description = "拼团商品", requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    private List<SalesCombinationProductBaseVO> products;

}
