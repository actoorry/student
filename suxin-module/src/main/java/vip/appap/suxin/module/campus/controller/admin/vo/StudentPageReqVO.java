package vip.appap.suxin.module.campus.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import vip.appap.suxin.framework.common.pojo.PageParam;

@Schema(description = "管理后台 - 学生分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentPageReqVO extends PageParam {

    @Schema(description = "学号", example = "2026001")
    private String studentNo;

    @Schema(description = "姓名", example = "张三")
    private String name;

    @Schema(description = "性别：1=男 2=女", example = "1")
    private Integer gender;

    @Schema(description = "班级", example = "高一（1）班")
    private String className;

    @Schema(description = "当前学年，如 2026-2027", example = "2026-2027")
    private String schoolYear;

    @Schema(description = "当前学期：1=第一学期 2=第二学期", example = "1")
    private Integer semester;

}
