package vip.appap.suxin.module.campus.dal.mysql;

import vip.appap.suxin.framework.mybatis.core.mapper.BaseMapperX;
import vip.appap.suxin.module.campus.dal.dataobject.StudentCourseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 选课 Mapper
 *
 * @author 书心软件
 */
@Mapper
public interface StudentCourseMapper extends BaseMapperX<StudentCourseDO> {

    default StudentCourseDO selectByStudentIdAndCourseId(Long studentId, Long courseId) {
        return selectOne(StudentCourseDO::getStudentId, studentId, StudentCourseDO::getCourseId, courseId);
    }

    default List<StudentCourseDO> selectListByStudentId(Long studentId) {
        return selectList(StudentCourseDO::getStudentId, studentId);
    }

}
