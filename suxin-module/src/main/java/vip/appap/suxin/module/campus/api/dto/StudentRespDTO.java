package vip.appap.suxin.module.campus.api.dto;

import lombok.Data;

/**
 * 学生信息 Response DTO
 *
 * @author 书心软件
 */
@Data
public class StudentRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 班级
     */
    private String className;

}
