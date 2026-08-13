package vip.appap.suxin.module.product.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 微信虚拟支付道具同步 Request VO")
@Data
public class ProductWechatVirtualGoodsSyncReqVO {

    @Schema(description = "SPU 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "SPU 编号不能为空")
    private Long spuId;

    @Schema(description = "支付应用编号，不传时使用当前租户第一个 wx_virtual_lite 渠道", example = "1")
    private Long appId;

}
