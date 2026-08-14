package vip.appap.suxin.module.campus.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 课程 DO
 *
 * @author 书心软件
 */
@TableName("campus_course")
@KeySequence("campus_course_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程编号，唯一
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
     * 授课教师（campus_teacher.id）
     */
    private Long teacherId;

}
