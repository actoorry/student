package vip.appap.suxin.module.campus.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 学生 DO
 *
 * @author 书心软件
 */
@TableName("campus_student")
@KeySequence("campus_student_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学号，唯一
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别：1=男 2=女
     */
    private Integer gender;

    /**
     * 班级
     */
    private String className;

    /**
     * 关联登录账号（system_users.id），用于学生端数据权限
     */
    private Long userId;

    /**
     * 手机号
     */
    private String mobile;

}
