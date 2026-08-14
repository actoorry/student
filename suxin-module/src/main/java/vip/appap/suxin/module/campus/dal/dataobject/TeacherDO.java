package vip.appap.suxin.module.campus.dal.dataobject;

import vip.appap.suxin.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 教师 DO
 *
 * @author 书心软件
 */
@TableName("campus_teacher")
@KeySequence("campus_teacher_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDO extends BaseDO {

    /**
     * 编号，数据库自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工号，唯一
     */
    private String teacherNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别：1=男 2=女
     */
    private Integer gender;

    /**
     * 职称
     */
    private String title;

    /**
     * 关联登录账号（system_users.id），用于老师端身份识别
     */
    private Long userId;

    /**
     * 手机号
     */
    private String mobile;

}
