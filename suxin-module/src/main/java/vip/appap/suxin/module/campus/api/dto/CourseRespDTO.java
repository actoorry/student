package vip.appap.suxin.module.campus.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程信息 Response DTO
 *
 * @author 书心软件
 */
@Data
public class CourseRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 课程编号
     */
    private String courseCode;

    /**
     * 课程名
     */
    private String courseName;

    /**
     * 学分
     */
    private BigDecimal credit;

    /**
     * 授课教师编号
     */
    private Long teacherId;

}
