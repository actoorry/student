package vip.appap.suxin.module.hr.controller.admin.salary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 薪资导入预检 Response VO")
@Data
public class SalaryImportCheckRespVO {

    @Schema(description = "从文件名解析出的年份", example = "2025")
    private Integer year;

    @Schema(description = "从文件名解析出的月份", example = "12")
    private Integer month;

    @Schema(description = "Excel 数据行数（不含表头）", example = "688")
    private Integer totalRows;

    @Schema(description = "表头校验是否通过", example = "true")
    private Boolean headerValid;

    @Schema(description = "表头缺失的必需列名（校验失败时返回）")
    private List<String> missingHeaders;

    @Schema(description = "数据库已存在的重复记录数（同年月同工号）", example = "0")
    private Integer duplicateCount;

    @Schema(description = "重复的人员编号样例（最多 10 个）")
    private List<String> duplicateEmployeeNos;

}
