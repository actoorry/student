package vip.appap.suxin.module.hr.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 外出记录新增/修改 Request VO")
@Data
public class OutboundSaveReqVO {

    @Schema(description = "主键（更新时必填）", example = "1024")
    private Long id;

    @Schema(description = "关联员工 partner.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30316")
    @NotNull(message = "员工不能为空")
    private Long partnerId;

    @Schema(description = "外出类型 hr_outbound_type", requiredMode = Schema.RequiredMode.REQUIRED, example = "rural_support")
    @NotEmpty(message = "外出类型不能为空")
    private String recordType;

    @Schema(description = "省份", example = "安徽省")
    private String province;

    @Schema(description = "市", example = "淮南市")
    private String city;

    @Schema(description = "区/县", example = "凤台县")
    private String county;

    @Schema(description = "进修/培训单位 或 下乡支援单位", example = "安徽省立医院")
    private String organization;

    @Schema(description = "进修/培训名称", example = "急救技能提升培训")
    private String practiceName;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "开始日期不能为空")
    private LocalDateTime startDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "结束日期不能为空")
    private LocalDateTime endDate;

    @Schema(description = "继教学分（进修/学习/培训填写）", example = "5.0")
    private BigDecimal continuingEducationCredit;

    @Schema(description = "总结")
    private String summary;

}
