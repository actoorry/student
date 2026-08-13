package vip.appap.suxin.module.sales.controller.admin.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理后台 - 电子面单账户创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SalesElectronicWaybillAccountCreateReqVO extends SalesElectronicWaybillAccountBaseVO {

}
