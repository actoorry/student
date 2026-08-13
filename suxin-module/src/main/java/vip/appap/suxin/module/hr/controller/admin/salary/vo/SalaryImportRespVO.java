package vip.appap.suxin.module.hr.controller.admin.salary.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - 薪资导入结果 Response VO")
@Data
@Builder
public class SalaryImportRespVO {

    @Schema(description = "年份", example = "2025")
    private Integer year;

    @Schema(description = "月份", example = "12")
    private Integer month;

    @Schema(description = "新增成功条数", example = "680")
    private Integer insertCount;

    @Schema(description = "覆盖更新成功条数", example = "8")
    private Integer updateCount;

    @Schema(description = "失败条数", example = "0")
    private Integer failureCount;

    @Schema(description = "失败明细，key 为行标识（工号或行号），value 为失败原因")
    private Map<String, String> failureRows;

}
