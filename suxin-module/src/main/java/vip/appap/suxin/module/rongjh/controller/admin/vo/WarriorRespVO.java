package vip.appap.suxin.module.rongjh.controller.admin.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 战友会身份 Response VO")
@Data
public class WarriorRespVO {

    @Schema(description = "会员编号(partner.id)", example = "1024")
    private Long id;

    @Schema(description = "会员昵称")
    private String nickname;

    @Schema(description = "会员手机号")
    private String mobile;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "身份证号（脱敏）")
    private String idCard;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "省份区域编号")
    private Integer stateId;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "军种")
    private String militaryBranch;

    @Schema(description = "服役年限")
    private Integer serviceYears;

    @Schema(description = "服役单位")
    private String serviceUnit;

    @Schema(description = "服役年份")
    private String serviceYear;

    @Schema(description = "入会说明")
    private String description;

    @Schema(description = "证件照片")
    private String certificateImg;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "审核备注")
    private String remark;

    @Schema(description = "申请/创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
