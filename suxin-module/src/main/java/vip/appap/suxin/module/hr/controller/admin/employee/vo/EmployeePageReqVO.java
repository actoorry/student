package vip.appap.suxin.module.hr.controller.admin.employee.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import vip.appap.suxin.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static vip.appap.suxin.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 员工分页 Request VO")
@Data
public class EmployeePageReqVO extends PageParam {

    @Schema(description = "关联 partner.id", example = "30316")
    private Long partnerId;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "员工手机号（人事专用）")
    private String employeeMobile;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "所在部门（关联 system_dept.id）")
    private Long dept;

    @Schema(description = "岗位编号，关联 system_post.id")
    private Long postId;

    @Schema(description = "人员类别")
    private String personnelCategory;

    @Schema(description = "职称")
    private String professionalTitle;

    @Schema(description = "政治面貌", example = "1")
    private String politicalStatus;

    @Schema(description = "入党时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] partyJoinDate;

    @Schema(description = "参加工作时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] careerStartDate;

    @Schema(description = "进入单位时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] hireDate;

    @Schema(description = "岗位")
    private String position;

    @Schema(description = "职务")
    private String duty;

    @Schema(description = "任职时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] appointmentDate;

    @Schema(description = "最高学历")
    private String highestEducation;

    @Schema(description = "全日制学历")
    private String fullTimeEducation;

    @Schema(description = "是否在编", example = "1")
    private String establishmentStatus;

    @Schema(description = "人员身份")
    private String personnelIdentity;

    @Schema(description = "人员来源")
    private String recruitmentSource;

    @Schema(description = "进入形式")
    private String entryMode;

    @Schema(description = "在职状态", example = "1")
    private String employmentStatus;

    @Schema(description = "民族")
    private String ethnicity;

    @Schema(description = "籍贯-省")
    private String nativeProvince;

    @Schema(description = "籍贯-市")
    private String nativeCity;

    @Schema(description = "银行卡卡号")
    private String bankCard;

    @Schema(description = "家庭信息")
    private String homeInformation;

    @Schema(description = "毕业院校及专业")
    private String schoolMajor;

    @Schema(description = "职称医护（图表辅助）")
    private String professionalCategory;

    @Schema(description = "姓名简写")
    private String nameAbbreviation;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
