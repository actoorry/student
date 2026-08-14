package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.module.campus.api.dto.TeacherRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 教师 API 接口
 *
 * @author 书心软件
 */
public interface TeacherApi {

    /**
     * 获得教师信息
     *
     * @param id 编号
     * @return 教师信息
     */
    TeacherRespDTO getTeacher(Long id);

    /**
     * 获得教师信息列表
     *
     * @param ids 编号集合
     * @return 教师信息列表
     */
    List<TeacherRespDTO> getTeacherList(Collection<Long> ids);

    /**
     * 获得教师信息 Map
     *
     * @param ids 编号集合
     * @return 教师信息 Map，key 为编号
     */
    default Map<Long, TeacherRespDTO> getTeacherMap(Collection<Long> ids) {
        List<TeacherRespDTO> list = getTeacherList(ids);
        return convertMap(list, TeacherRespDTO::getId);
    }

    /**
     * 校验教师是否存在
     *
     * @param id 编号
     */
    void validateTeacher(Long id);

}
