package vip.appap.suxin.module.hr.controller.admin.certificate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 人员证书新增/修改 Request VO")
@Data
public class CertificateSaveReqVO {

    @Schema(description = "主键", example = "1024")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "10870")
    @NotNull(message = "关联员工不能为空")
    private Long partnerId;

    @Schema(description = "员工工号（选员工时带出）")
    private String employeeNo;

    @Schema(description = "所在部门（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "证书类型（字典 hr_certificate_type）", example = "ys_zyz")
    private String certificateType;

    @Schema(description = "证书名称")
    private String certificateName;

    @Schema(description = "证书编号")
    private String certificateNo;

    @Schema(description = "发证机关")
    private String issuingAuthority;

    @Schema(description = "发证日期")
    private LocalDateTime issueDate;

    @Schema(description = "到期日期（督查核心字段）")
    private LocalDateTime expireDate;

    @Schema(description = "上次考核日")
    private LocalDateTime lastAssessmentDate;

    @Schema(description = "下次考核日")
    private LocalDateTime nextAssessmentDate;

    @Schema(description = "证书文件 URL")
    private String attachment;

    @Schema(description = "备注")
    private String remark;

}
