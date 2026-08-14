package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.StudentApi;
import vip.appap.suxin.module.campus.api.TeacherApi;
import vip.appap.suxin.module.campus.controller.admin.vo.CoursePageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.CourseSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.CourseDO;
import vip.appap.suxin.module.campus.dal.dataobject.StudentCourseDO;
import vip.appap.suxin.module.campus.dal.mysql.CourseMapper;
import vip.appap.suxin.module.campus.dal.mysql.StudentCourseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static vip.appap.suxin.framework.common.exception.util.ServiceExceptionUtil.exception;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.COURSE_CODE_EXISTS;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.COURSE_NOT_EXISTS;
import static vip.appap.suxin.module.campus.enums.ErrorCodeConstants.STUDENT_COURSE_EXISTS;

/**
 * 课程 Service 实现类
 *
 * @author 书心软件
 */
@Service
@Validated
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private StudentCourseMapper studentCourseMapper;

    @Resource
    private StudentApi studentApi;

    @Resource
    private TeacherApi teacherApi;

    @Override
    public Long createCourse(CourseSaveReqVO createReqVO) {
        // 校验课程编号唯一
        validateCourseCodeUnique(null, createReqVO.getCourseCode());
        // 校验授课教师存在
        if (createReqVO.getTeacherId() != null) {
            teacherApi.validateTeacher(createReqVO.getTeacherId());
        }
        // 插入
        CourseDO course = BeanUtils.toBean(createReqVO, CourseDO.class);
        courseMapper.insert(course);
        return course.getId();
    }

    @Override
    public void updateCourse(CourseSaveReqVO updateReqVO) {
        // 校验存在
        validateCourseExists(updateReqVO.getId());
        // 校验课程编号唯一
        validateCourseCodeUnique(updateReqVO.getId(), updateReqVO.getCourseCode());
        // 校验授课教师存在
        if (updateReqVO.getTeacherId() != null) {
            teacherApi.validateTeacher(updateReqVO.getTeacherId());
        }
        // 更新
        CourseDO updateObj = BeanUtils.toBean(updateReqVO, CourseDO.class);
        courseMapper.updateById(updateObj);
    }

    @Override
    public void deleteCourse(Long id) {
        // 校验存在
        validateCourseExists(id);
        // 删除
        courseMapper.deleteById(id);
    }

    @Override
    public CourseDO getCourse(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public PageResult<CourseDO> getCoursePage(CoursePageReqVO pageReqVO) {
        return courseMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CourseDO> getCourseList(Collection<Long> ids) {
        return courseMapper.selectBatchIds(ids);
    }

    @Override
    public CourseDO validateCourseExists(Long id) {
        CourseDO course = courseMapper.selectById(id);
        if (course == null) {
            throw exception(COURSE_NOT_EXISTS);
        }
        return course;
    }

    @Override
    public void selectCourse(Long studentId, Long courseId) {
        // 校验学生与课程存在
        studentApi.validateStudent(studentId);
        validateCourseExists(courseId);
        // 校验是否已选课
        StudentCourseDO exist = studentCourseMapper.selectByStudentIdAndCourseId(studentId, courseId);
        if (exist != null) {
            throw exception(STUDENT_COURSE_EXISTS);
        }
        // 插入选课记录
        StudentCourseDO studentCourse = new StudentCourseDO()
                .setStudentId(studentId)
                .setCourseId(courseId);
        studentCourseMapper.insert(studentCourse);
    }

    private void validateCourseCodeUnique(Long id, String courseCode) {
        CourseDO course = courseMapper.selectByCourseCode(courseCode);
        if (course == null) {
            return;
        }
        if (id == null || !id.equals(course.getId())) {
            throw exception(COURSE_CODE_EXISTS, courseCode);
        }
    }

}
