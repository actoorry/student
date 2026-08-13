package vip.appap.suxin.module.rongjh.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 APP - 战友会/三属 提交 Request VO")
@Data
public class AppWarriorSubmitReqVO {

    @Schema(description = "类型: SELF/MARTYR/SACRIFICE/ILLNESS/FAMILY", example = "SELF")
    private String type;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "身份证号")
    @JsonProperty("id_card")
    private String idCard;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "省份名称")
    private String province;

    @Schema(description = "省份区域编号")
    @JsonProperty("state_id")
    private Integer stateId;

    @Schema(description = "省份名称（前端表单字段）")
    @JsonProperty("state_name")
    private String stateName;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "军种")
    @JsonProperty("military_branch")
    private String militaryBranch;

    @Schema(description = "服役年限")
    @JsonProperty("service_years")
    private Integer serviceYears;

    @Schema(description = "服役单位")
    @JsonProperty("service_unit")
    private String serviceUnit;

    @Schema(description = "服役年份")
    @JsonProperty("service_year")
    private String serviceYear;

    @Schema(description = "入会说明")
    private String description;

    @Schema(description = "证件照片")
    @JsonProperty("certificate_img")
    private String certificateImg;

}
