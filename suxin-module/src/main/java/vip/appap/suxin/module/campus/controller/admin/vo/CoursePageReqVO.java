package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vip.appap.suxin.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 课程分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CoursePageReqVO extends PageParam {

    @Schema(description = "课程编号", example = "C001")
    private String courseCode;

    @Schema(description = "课程名", example = "高等数学")
    private String courseName;

    @Schema(description = "授课教师编号", example = "1")
    private Long teacherId;

}
