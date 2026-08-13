package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 限时折扣活动创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesDiscountActivityCreateReqVO extends SalesDiscountActivityBaseVO {

    /**
     * 商品列表
     */
    @NotEmpty(message = "商品列表不能为空")
    @Valid
    private List<Product> products;

}
