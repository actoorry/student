package vip.appap.suxin.module.campus.service;

import vip.appap.suxin.framework.common.pojo.PageResult;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherPageReqVO;
import vip.appap.suxin.module.campus.controller.admin.vo.TeacherSaveReqVO;
import vip.appap.suxin.module.campus.dal.dataobject.TeacherDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * 教师 Service 接口
 *
 * @author 书心软件
 */
public interface TeacherService {

    /**
     * 创建教师
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTeacher(@Valid TeacherSaveReqVO createReqVO);

    /**
     * 更新教师
     *
     * @param updateReqVO 更新信息
     */
    void updateTeacher(@Valid TeacherSaveReqVO updateReqVO);

    /**
     * 删除教师
     *
     * @param id 编号
     */
    void deleteTeacher(Long id);

    /**
     * 获得教师
     *
     * @param id 编号
     * @return 教师
     */
    TeacherDO getTeacher(Long id);

    /**
     * 获得教师分页
     *
     * @param pageReqVO 分页查询
     * @return 教师分页
     */
    PageResult<TeacherDO> getTeacherPage(TeacherPageReqVO pageReqVO);

    /**
     * 获得教师列表
     *
     * @param ids 编号集合
     * @return 教师列表
     */
    List<TeacherDO> getTeacherList(Collection<Long> ids);

    /**
     * 校验教师是否存在
     *
     * @param id 编号
     * @return 教师
     */
    TeacherDO validateTeacherExists(Long id);

}
