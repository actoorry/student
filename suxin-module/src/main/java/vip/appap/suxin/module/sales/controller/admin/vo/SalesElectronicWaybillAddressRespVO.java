package vip.appap.suxin.module.sales.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理后台 - 商户寄件地址（用于电子面单账户表单选择）
 */
@Schema(description = "管理后台 - 商户寄件地址 Response VO")
@Data
public class SalesElectronicWaybillAddressRespVO {

    @Schema(description = "地址编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "收件人名称（寄件人）", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15601691300")
    private String mobile;

    @Schema(description = "地区编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "4371")
    private Integer areaId;

    @Schema(description = "地区名称", example = "上海 上海市 静安区")
    private String areaName;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "XX路XX号")
    private String detailAddress;

}
