package vip.appap.suxin.module.campus.api;

import vip.appap.suxin.module.campus.api.dto.StudentRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static vip.appap.suxin.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 学生 API 接口
 *
 * @author 书心软件
 */
public interface StudentApi {

    /**
     * 获得学生信息
     *
     * @param id 编号
     * @return 学生信息
     */
    StudentRespDTO getStudent(Long id);

    /**
     * 获得学生信息列表
     *
     * @param ids 编号集合
     * @return 学生信息列表
     */
    List<StudentRespDTO> getStudentList(Collection<Long> ids);

    /**
     * 获得学生信息 Map
     *
     * @param ids 编号集合
     * @return 学生信息 Map，key 为编号
     */
    default Map<Long, StudentRespDTO> getStudentMap(Collection<Long> ids) {
        List<StudentRespDTO> list = getStudentList(ids);
        return convertMap(list, StudentRespDTO::getId);
    }

    /**
     * 校验学生是否存在
     *
     * @param id 编号
     */
    void validateStudent(Long id);

    /**
     * 根据登录账号获取学生
     *
     * @param userId 登录账号（system_users.id）
     * @return 学生信息，可能为 null
     */
    StudentRespDTO getStudentByUserId(Long userId);

}
