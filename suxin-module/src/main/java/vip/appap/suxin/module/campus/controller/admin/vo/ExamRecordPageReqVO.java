package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vip.appap.suxin.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 考试记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamRecordPageReqVO extends PageParam {

    @Schema(description = "学生编号", example = "1")
    private Long studentId;

    @Schema(description = "课程编号", example = "1")
    private Long courseId;

    @Schema(description = "学年，如 2026-2027", example = "2026-2027")
    private String schoolYear;

    @Schema(description = "学期：1=第一学期 2=第二学期", example = "1")
    private Integer semester;

}
