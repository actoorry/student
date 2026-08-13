package vip.appap.suxin.module.system.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - IP/定位解析城市 Response VO")
@Data
public class AppAreaLocationRespVO {

    @Schema(description = "城市编号", example = "340100")
    private Integer id;

    @Schema(description = "城市名称", example = "合肥市")
    private String name;

    @Schema(description = "完整地址", example = "安徽省 合肥市")
    private String address;

    @Schema(description = "省份编号", example = "340000")
    private Integer provinceId;

    @Schema(description = "省份名称", example = "安徽省")
    private String provinceName;

}
