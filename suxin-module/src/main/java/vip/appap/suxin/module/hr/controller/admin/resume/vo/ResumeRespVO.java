package vip.appap.suxin.module.hr.controller.admin.resume.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 履历 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ResumeRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10675")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "关联员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "16087")
    @ExcelProperty("关联员工")
    private Long partnerId;

    @Schema(description = "姓名", example = "张三")
    @ExcelProperty("姓名")
    private String name;

    @Schema(description = "员工工号")
    @ExcelProperty("员工工号")
    private String employeeNo;

    @Schema(description = "所在部门（部门 ID）")
    private String dept;

    @Schema(description = "所在部门名称")
    @ExcelProperty("所在部门")
    private String deptName;

    @Schema(description = "个人简历")
    @ExcelProperty("个人简历")
    private String resumeContent;

    @Schema(description = "奖惩情况")
    @ExcelProperty("奖惩情况")
    private String awardsPunishments;

    @Schema(description = "证书")
    @ExcelProperty("证书")
    private String certificates;

    @Schema(description = "论文")
    @ExcelProperty("论文")
    private String papers;

    @Schema(description = "年度考核情况")
    @ExcelProperty("年度考核情况")
    private String annualReview;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "省份")
    @ExcelProperty("省份")
    private String province;

    @Schema(description = "市")
    @ExcelProperty("市")
    private String city;

    @Schema(description = "县")
    @ExcelProperty("县")
    private String county;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
