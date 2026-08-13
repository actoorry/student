package vip.appap.suxin.module.hr.controller.admin.employee.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 员工 Response VO")
@Data
@ExcelIgnoreUnannotated
public class EmployeeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14241")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联 partner.id", requiredMode = Schema.RequiredMode.REQUIRED, example = "30316")
    @ExcelProperty("关联 partner.id")
    private Long partnerId;

    @Schema(description = "姓名")
    @ExcelProperty("姓名")
    private String name;

    @Schema(description = "员工手机号（人事专用）")
    @ExcelProperty("员工手机号")
    private String employeeMobile;

    @Schema(description = "身份证号")
    @ExcelProperty("身份证号")
    private String idCard;

    @Schema(description = "性别")
    @ExcelProperty("性别")
    private Integer sex;

    @Schema(description = "出生日期")
    @ExcelProperty("出生日期")
    private LocalDateTime birthday;

    @Schema(description = "部门名称")
    @ExcelProperty("部门名称")
    private String deptName;

    @Schema(description = "员工工号")
    @ExcelProperty("员工工号")
    private String employeeNo;

    @Schema(description = "所在部门（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "岗位编号，关联 system_post.id")
    private Long postId;

    @Schema(description = "岗位名称")
    @ExcelProperty("岗位名称")
    private String postName;

    @Schema(description = "人员类别")
    @ExcelProperty("人员类别")
    private String personnelCategory;

    @Schema(description = "职称")
    @ExcelProperty("职称")
    private String professionalTitle;

    @Schema(description = "政治面貌", example = "1")
    @ExcelProperty("政治面貌")
    private String politicalStatus;

    @Schema(description = "入党时间")
    @ExcelProperty("入党时间")
    private LocalDateTime partyJoinDate;

    @Schema(description = "参加工作时间")
    @ExcelProperty("参加工作时间")
    private LocalDateTime careerStartDate;

    @Schema(description = "进入单位时间")
    @ExcelProperty("进入单位时间")
    private LocalDateTime hireDate;

    @Schema(description = "岗位")
    @ExcelProperty("岗位")
    private String position;

    @Schema(description = "职务")
    @ExcelProperty("职务")
    private String duty;

    @Schema(description = "任职时间")
    @ExcelProperty("任职时间")
    private LocalDateTime appointmentDate;

    @Schema(description = "最高学历")
    @ExcelProperty("最高学历")
    private String highestEducation;

    @Schema(description = "全日制学历")
    @ExcelProperty("全日制学历")
    private String fullTimeEducation;

    @Schema(description = "是否在编", example = "1")
    @ExcelProperty("是否在编")
    private String establishmentStatus;

    @Schema(description = "人员身份")
    @ExcelProperty("人员身份")
    private String personnelIdentity;

    @Schema(description = "人员来源")
    @ExcelProperty("人员来源")
    private String recruitmentSource;

    @Schema(description = "进入形式")
    @ExcelProperty("进入形式")
    private String entryMode;

    @Schema(description = "在职状态", example = "1")
    @ExcelProperty("在职状态")
    private String employmentStatus;

    @Schema(description = "民族")
    @ExcelProperty("民族")
    private String ethnicity;

    @Schema(description = "籍贯-省")
    @ExcelProperty("籍贯-省")
    private String nativeProvince;

    @Schema(description = "籍贯-市")
    @ExcelProperty("籍贯-市")
    private String nativeCity;

    @Schema(description = "银行卡卡号")
    @ExcelProperty("银行卡卡号")
    private String bankCard;

    @Schema(description = "家庭信息")
    @ExcelProperty("家庭信息")
    private String homeInformation;

    @Schema(description = "毕业院校及专业")
    @ExcelProperty("毕业院校及专业")
    private String schoolMajor;

    @Schema(description = "职称医护（图表辅助）")
    @ExcelProperty("职称医护（图表辅助）")
    private String professionalCategory;

    @Schema(description = "姓名简写")
    @ExcelProperty("姓名简写")
    private String nameAbbreviation;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
