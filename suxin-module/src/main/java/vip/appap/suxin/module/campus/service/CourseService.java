package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.campus.controller.admin.vo.CoursePageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.CourseSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.CourseDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 课程 Service 接口
 *
 * @author 书心软件
 */
public interface CourseService {

    /**
     * 创建课程
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCourse(@Valid CourseSaveReqVO createReqVO);

    /**
     * 更新课程
     *
     * @param updateReqVO 更新信息
     */
    void updateCourse(@Valid CourseSaveReqVO updateReqVO);

    /**
     * 删除课程
     *
     * @param id 编号
     */
    void deleteCourse(Long id);

    /**
     * 获得课程
     *
     * @param id 编号
     * @return 课程
     */
    CourseDO getCourse(Long id);

    /**
     * 获得课程分页
     *
     * @param pageReqVO 分页查询
     * @return 课程分页
     */
    PageResult<CourseDO> getCoursePage(CoursePageReqVO pageReqVO);

    /**
     * 获得课程列表
     *
     * @param ids 编号集合
     * @return 课程列表
     */
    List<CourseDO> getCourseList(Collection<Long> ids);

    /**
     * 校验课程是否存在
     *
     * @param id 编号
     * @return 课程
     */
    CourseDO validateCourseExists(Long id);

    /**
     * 学生选课
     *
     * @param studentId 学生编号
     * @param courseId  课程编号
     */
    void selectCourse(Long studentId, Long courseId);

}
