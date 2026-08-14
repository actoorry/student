package vip.appap.suxin.module.campus.api.dto;

import lombok.Data;

/**
 * 教师信息 Response DTO
 *
 * @author 书心软件
 */
@Data
public class TeacherRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 工号
     */
    private String teacherNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 职称
     */
    private String title;

}
