package vip.appap.suxin.module.sales.controller.admin.vo;

import vip.appap.suxin.framework.common.enums.CommonStatusEnum;
import vip.appap.suxin.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 电子面单账户 Base VO，提供给添加、修改、详细的子 VO 使用
 * <p>
 * 敏感凭据字段（partnerKey / partnerSecret / partnerId / net 等）在响应中一律脱敏；
 * 更新时空值表示保留原凭据，因此创建/更新分别校验。
 */
@Data
public class SalesElectronicWaybillAccountBaseVO {

    @Schema(description = "账户名称（租户内唯一）", requiredMode = Schema.RequiredMode.REQUIRED, example = "顺丰月结账户")
    @NotBlank(message = "账户名称不能为空")
    @Size(max = 64, message = "账户名称长度不能超过 64")
    private String name;

    @Schema(description = "快递公司编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "快递公司不能为空")
    private Long expressId;

    @Schema(description = "快递100平台授权 key（AES 加密落库，响应脱敏；更新为空表示保留原值）", example = "")
    @Size(max = 128, message = "平台授权 key 长度不能超过 128")
    private String key;

    @Schema(description = "快递100平台授权密钥（AES 加密落库，响应脱敏；更新为空表示保留原值）", example = "")
    @Size(max = 128, message = "平台授权密钥长度不能超过 128")
    private String secret;

    @Schema(description = "电子面单月结账号", example = "K1234567890")
    @Size(max = 64, message = "月结账号长度不能超过 64")
    private String partnerId;

    @Schema(description = "电子面单密码（AES 加密落库，响应脱敏；更新为空表示保留原值）", example = "")
    @Size(max = 128, message = "电子面单密码长度不能超过 128")
    private String partnerKey;

    @Schema(description = "电子面单密钥（AES 加密落库，响应脱敏；更新为空表示保留原值）", example = "")
    @Size(max = 128, message = "电子面单密钥长度不能超过 128")
    private String partnerSecret;

    @Schema(description = "电子面单网点", example = "taobao")
    @Size(max = 64, message = "网点长度不能超过 64")
    private String net;

    @Schema(description = "电子面单承载编号", example = "001")
    @Size(max = 64, message = "承载编号长度不能超过 64")
    private String code;

    @Schema(description = "电子面单客户账户名称", example = "书心网络科技有限公司")
    @Size(max = 64, message = "客户账户名称长度不能超过 64")
    private String partnerName;

    @Schema(description = "业务员编码（部分快递公司取消面单必填）", example = "C001")
    @Size(max = 64, message = "业务员编码长度不能超过 64")
    private String checkMan;

    @Schema(description = "产品业务类型（部分快递公司取消面单必填）", example = "标准快递")
    @Size(max = 64, message = "产品业务类型长度不能超过 64")
    private String expType;

    @Schema(description = "快递100模板 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "60f6c17c7c223700131d8bc3")
    @NotBlank(message = "快递100模板 ID 不能为空")
    @Size(max = 64, message = "模板 ID 长度不能超过 64")
    private String tempId;

    @Schema(description = "默认寄件地址编号（partner_address.id）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "默认寄件地址不能为空")
    private Long defaultAddressId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
