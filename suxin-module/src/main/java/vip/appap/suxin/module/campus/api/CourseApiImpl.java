package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.framework.common.util.object.BeanUtils;
import vip.appap.suxin.module.campus.api.dto.CourseRespDTO;
import vip.appap.suxin.module.campus.dal.dataobject.CourseDO;
import vip.appap.suxin.module.campus.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 课程 API 实现类
 *
 * @author 书心软件
 */
@Service
public class CourseApiImpl implements CourseApi {

    @Resource
    private CourseService courseService;

    @Override
    public CourseRespDTO getCourse(Long id) {
        CourseDO course = courseService.getCourse(id);
        return BeanUtils.toBean(course, CourseRespDTO.class);
    }

    @Override
    public List<CourseRespDTO> getCourseList(Collection<Long> ids) {
        return BeanUtils.toBean(courseService.getCourseList(ids), CourseRespDTO.class);
    }

    @Override
    public void validateCourse(Long id) {
        courseService.validateCourseExists(id);
    }

}
