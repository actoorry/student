package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.module.campus.api.dto.CourseRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 课程 API 接口
 *
 * @author 书心软件
 */
public interface CourseApi {

    /**
     * 获得课程信息
     *
     * @param id 编号
     * @return 课程信息
     */
    CourseRespDTO getCourse(Long id);

    /**
     * 获得课程信息列表
     *
     * @param ids 编号集合
     * @return 课程信息列表
     */
    List<CourseRespDTO> getCourseList(Collection<Long> ids);

    /**
     * 获得课程信息 Map
     *
     * @param ids 编号集合
     * @return 课程信息 Map，key 为编号
     */
    default Map<Long, CourseRespDTO> getCourseMap(Collection<Long> ids) {
        List<CourseRespDTO> list = getCourseList(ids);
        return convertMap(list, CourseRespDTO::getId);
    }

    /**
     * 校验课程是否存在
     *
     * @param id 编号
     */
    void validateCourse(Long id);

}
