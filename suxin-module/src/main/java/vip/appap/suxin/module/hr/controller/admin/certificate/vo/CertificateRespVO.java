package vip.appap.suxin.module.hr.controller.admin.certificate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import vip.appap.suxin.module.hr.convert.certificate.CertificateStatusConvert;

@Schema(description = "管理后台 - 人员证书 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CertificateRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "10870")
    @ExcelProperty("关联员工")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    @ExcelProperty("姓名")
    private String name;

    @Schema(description = "员工工号")
    @ExcelProperty("员工工号")
    private String employeeNo;

    @Schema(description = "科室")
    @ExcelProperty("科室")
    private String deptName;

    @Schema(description = "科室 id（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "证书类型", example = "ys_zyz")
    @ExcelProperty("证书类型")
    private String certificateType;

    @Schema(description = "证书名称")
    @ExcelProperty("证书名称")
    private String certificateName;

    @Schema(description = "证书编号")
    @ExcelProperty("证书编号")
    private String certificateNo;

    @Schema(description = "发证机关")
    @ExcelProperty("发证机关")
    private String issuingAuthority;

    @Schema(description = "发证日期")
    @ExcelProperty("发证日期")
    private LocalDateTime issueDate;

    @Schema(description = "到期日期")
    @ExcelProperty("到期日期")
    private LocalDateTime expireDate;

    @Schema(description = "上次考核日")
    @ExcelProperty("上次考核日")
    private LocalDateTime lastAssessmentDate;

    @Schema(description = "下次考核日")
    @ExcelProperty("下次考核日")
    private LocalDateTime nextAssessmentDate;

    @Schema(description = "证书文件 URL")
    private String attachment;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "计算状态：valid 有效 / expiring 30天内到期 / expiring_60 60天内到期 / expiring_90 90天内到期 / expired 已过期 / assessment_overdue 考核逾期 / assessment_due_60 考核将到期", example = "expired")
    @ExcelProperty(value = "状态", converter = CertificateStatusConvert.class)
    private String status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
