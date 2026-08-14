package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vip.appap.suxin.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 教师分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherPageReqVO extends PageParam {

    @Schema(description = "工号", example = "T001")
    private String teacherNo;

    @Schema(description = "姓名", example = "李四")
    private String name;

    @Schema(description = "性别：1=男 2=女", example = "2")
    private Integer gender;

    @Schema(description = "职称", example = "教授")
    private String title;

}
