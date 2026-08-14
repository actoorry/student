package vip.appap.suxin.module.campus.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 考试记录 DO
 *
 * @author 书心软件
 */
@TableName("campus_exam_record")
@KeySequence("campus_exam_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRecordDO extends BaseDO {

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

    /**
     * 学年，如 2026-2027
     */
    private String schoolYear;

    /**
     * 学期：1=第一学期 2=第二学期
     */
    private Integer semester;

    /**
     * 分数
     */
    private BigDecimal score;

}
