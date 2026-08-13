package vip.appap.suxin.module.rongjh.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import vip.appap.suxin.framework.common.validation.Mobile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "用户 APP - 爱心帮扶申请 Request VO")
@Data
public class AppHelpSubmitReqVO {

    @Schema(description = "困难人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "联系电话", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系电话不能为空")
    @Mobile(message = "联系电话格式不正确")
    private String phone;

    @Schema(description = "身份证号")
    @JsonProperty("id_card")
    private String idCard;

    @Schema(description = "省份编号")
    @JsonProperty("province_id")
    private Integer provinceId;

    @Schema(description = "所在地区")
    private String city;

    @Schema(description = "困难原因/求助说明")
    private String reason;

    @Schema(description = "求助说明（前端字段 description）")
    private String description;

    @Schema(description = "申请金额")
    @JsonProperty("apply_amount")
    private BigDecimal applyAmount;

    @Schema(description = "证明材料附件")
    private List<Map<String, Object>> attachments;

}
