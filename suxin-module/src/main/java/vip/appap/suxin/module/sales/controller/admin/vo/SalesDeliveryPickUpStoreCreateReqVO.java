package vip.appap.suxin.module.sales.controller.admin.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 自提门店创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesDeliveryPickUpStoreCreateReqVO extends SalesDeliveryPickUpStoreBaseVO {

}
