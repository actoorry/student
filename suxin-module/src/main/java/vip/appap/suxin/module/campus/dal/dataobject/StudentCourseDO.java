package vip.appap.suxin.module.campus.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 选课 DO（学生-课程 多对多中间表）
 *
 * @author 书心软件
 */
@TableName("campus_student_course")
@KeySequence("campus_student_course_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生编号（campus_student.id）
     */
    private Long studentId;

    /**
     * 课程编号（campus_course.id）
     */
    private Long courseId;

}
